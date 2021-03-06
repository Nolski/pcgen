/*
 * KitPanel.java
 * Copyright James Dempsey, 2012
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 01/03/2012 8:01:51 AM
 *
 * $Id$
 */
package pcgen.gui2.kits;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pcgen.facade.core.CharacterFacade;
import pcgen.facade.core.KitFacade;
import pcgen.facade.util.event.ListEvent;
import pcgen.facade.util.event.ListListener;
import pcgen.facade.util.DefaultListFacade;
import pcgen.facade.util.ListFacade;
import pcgen.gui2.filter.Filter;
import pcgen.gui2.filter.FilterBar;
import pcgen.gui2.filter.FilterButton;
import pcgen.gui2.filter.FilterUtilities;
import pcgen.gui2.filter.FilteredListFacade;
import pcgen.gui2.filter.FilteredTreeViewTable;
import pcgen.gui2.filter.SearchFilterPanel;
import pcgen.gui2.tabs.models.QualifiedTreeCellRenderer;
import pcgen.gui2.tools.FlippingSplitPane;
import pcgen.gui2.tools.Icons;
import pcgen.gui2.tools.InfoPane;
import pcgen.gui2.util.treeview.DataView;
import pcgen.gui2.util.treeview.DataViewColumn;
import pcgen.gui2.util.treeview.DefaultDataViewColumn;
import pcgen.gui2.util.treeview.TreeView;
import pcgen.gui2.util.treeview.TreeViewModel;
import pcgen.gui2.util.treeview.TreeViewPath;
import pcgen.system.LanguageBundle;

/**
 * The Class <code>KitPanel</code> displays an available/selected table pair to 
 * allow the allocation of kit to the currently selected character. 
 *
 * <br>
 * 
 * @author James Dempsey &lt;jdempsey@users.sourceforge.net&gt;
 */
@SuppressWarnings("serial")
public class KitPanel extends FlippingSplitPane
{

	private final FilteredTreeViewTable<Object, KitFacade> availableTable;
	private final FilteredTreeViewTable<Object, KitFacade> selectedTable;
	private final JButton addButton;
	private final InfoPane infoPane;
	private final CharacterFacade character;
	private QualifiedTreeCellRenderer renderer;
	private AddAction addAction;
	private final FilterButton<Object, KitFacade> qFilterButton;

	/**
	 * Create a new instance of KitPanel for a character.
	 * @param character The character being displayed.
	 */
	public KitPanel(CharacterFacade character)
	{
		super("kit");
		this.character = character;
		this.availableTable = new FilteredTreeViewTable<>();
		this.selectedTable = new FilteredTreeViewTable<>();
		this.addButton = new JButton();
		this.infoPane = new InfoPane(LanguageBundle.getString("in_kitInfo")); //$NON-NLS-1$
		this.renderer = new QualifiedTreeCellRenderer();
		this.addAction = new AddAction(character);
		this.qFilterButton = new FilterButton<>("KitQualified");

		initComponents();
		initDefaults();
	}

	private void initComponents()
	{
		renderer.setCharacter(character);
		FlippingSplitPane topPane = new FlippingSplitPane("kitTop");
		setTopComponent(topPane);
		setOrientation(VERTICAL_SPLIT);

		FilterBar<Object, KitFacade> bar = new FilterBar<>();
		bar.addDisplayableFilter(new SearchFilterPanel());
		qFilterButton.setText(LanguageBundle.getString("in_igQualFilter")); //$NON-NLS-1$
		bar.addDisplayableFilter(qFilterButton);

		availableTable.setTreeViewModel(new KitTreeViewModel(character, true));
		availableTable.setTreeCellRenderer(renderer);

		JPanel availPanel = FilterUtilities.configureFilteredTreeViewPane(availableTable, bar);

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		addButton.setHorizontalTextPosition(SwingConstants.LEADING);
		addButton.setAction(addAction);
		box.add(addButton);
		box.add(Box.createHorizontalStrut(5));
		box.setBorder(new EmptyBorder(0, 0, 5, 0));
		availPanel.add(box, BorderLayout.SOUTH);

		topPane.setLeftComponent(availPanel);

		JPanel selPanel = new JPanel(new BorderLayout());
		FilterBar<Object, KitFacade> filterBar = new FilterBar<>();
		filterBar.addDisplayableFilter(new SearchFilterPanel());

		selectedTable.setDisplayableFilter(filterBar);
		selectedTable.setTreeViewModel(new KitTreeViewModel(character, false));
		selectedTable.setTreeCellRenderer(renderer);
		selPanel.add(new JScrollPane(selectedTable), BorderLayout.CENTER);

		topPane.setRightComponent(selPanel);
		setBottomComponent(infoPane);
		setResizeWeight(0.75);
	}

	/**
	 * 
	 */
	private void initDefaults()
	{
		InfoHandler infoHandler = new InfoHandler(character);
		availableTable.getSelectionModel().addListSelectionListener(infoHandler);
		selectedTable.getSelectionModel().addListSelectionListener(infoHandler);

		KitFilterHandler kitFilterHandler = new KitFilterHandler(character);
		kitFilterHandler.install();
		
		availableTable.addActionListener(addAction);
	}

	private class KitFilterHandler
	{

		private final Filter<Object, KitFacade> qFilter = new Filter<Object, KitFacade>()
		{
			@Override
			public boolean accept(Object context, KitFacade element)
			{
				return character.isQualifiedFor(element);
			}

		};
		private final CharacterFacade character;

		public KitFilterHandler(CharacterFacade character)
		{
			this.character = character;
		}

		public void install()
		{
			qFilterButton.setFilter(qFilter);
		}

	}

	private class InfoHandler implements ListSelectionListener
	{

		private CharacterFacade character;

		public InfoHandler(CharacterFacade character)
		{
			this.character = character;
		}

        @Override
		public void valueChanged(ListSelectionEvent e)
		{
			if (!e.getValueIsAdjusting())
			{
				Object obj = null;
				if (e.getSource() == availableTable.getSelectionModel())
				{
					int selectedRow = availableTable.getSelectedRow();
					if (selectedRow != -1)
					{
						obj = availableTable.getModel().getValueAt(selectedRow, 0);
					}
				}
				else
				{
					int selectedRow = selectedTable.getSelectedRow();
					if (selectedRow != -1)
					{
						obj = selectedTable.getModel().getValueAt(selectedRow, 0);
					}
				}
				if (obj instanceof KitFacade)
				{
					infoPane.setText(character.getInfoFactory().getHTMLInfo((KitFacade) obj));
				}
			}
		}

	}

	private class AddAction extends AbstractAction
	{

		private CharacterFacade character;

		public AddAction(CharacterFacade character)
		{
			super(LanguageBundle.getString("in_kitApply")); //$NON-NLS-1$
			this.character = character;
			putValue(SMALL_ICON, Icons.Forward16.getImageIcon());
		}

        @Override
		public void actionPerformed(ActionEvent e)
		{
			List<Object> data = availableTable.getSelectedData();
			for (Object kit : data)
			{
				if (kit instanceof KitFacade)
				{
					character.addKit((KitFacade) kit);
					return;
				}
			}
		}

	}

	private static class KitTreeViewModel
			implements TreeViewModel<KitFacade>, DataView<KitFacade>,
			Filter<CharacterFacade, KitFacade>, ListListener<KitFacade>
	{

		private static final DefaultListFacade<? extends TreeView<KitFacade>> treeViews =
				new DefaultListFacade<TreeView<KitFacade>>(Arrays.asList(KitTreeView.values()));
		private final List<DefaultDataViewColumn> columns;
		private final CharacterFacade character;
		private final boolean isAvailModel;
		private FilteredListFacade<CharacterFacade, KitFacade> kits;

		public KitTreeViewModel(CharacterFacade character, boolean isAvailModel)
		{
			this.character = character;
			this.isAvailModel = isAvailModel;
			if (isAvailModel)
			{
				kits = new FilteredListFacade<>();
				kits.setContext(character);
				kits.setFilter(this);
				ListFacade<KitFacade> kitList = new DefaultListFacade<>(character.getAvailableKits());
				kits.setDelegate(kitList);
				character.getKits().addListListener(this);
				columns = Arrays.asList(new DefaultDataViewColumn("in_descrip", String.class, false), //$NON-NLS-1$
						new DefaultDataViewColumn("in_source", String.class, false)); //$NON-NLS-1$
			}
			else
			{
				kits = null;
				columns = Arrays.asList(new DefaultDataViewColumn("in_descrip", String.class, false), //$NON-NLS-1$
						new DefaultDataViewColumn("in_source", String.class, false)); //$NON-NLS-1$
			}
		}

        @Override
		public ListFacade<? extends TreeView<KitFacade>> getTreeViews()
		{
			return treeViews;
		}

        @Override
		public int getDefaultTreeViewIndex()
		{
			return 0;
		}

        @Override
		public DataView<KitFacade> getDataView()
		{
			return this;
		}

        @Override
		public ListFacade<KitFacade> getDataModel()
		{
			if (isAvailModel)
			{
				return kits;
			}
			else
			{
				return character.getKits();
			}
		}

		@Override
		public Object getData(KitFacade element, int column)
		{
			switch(column){
				case 0:
					return character.getInfoFactory().getDescription(element);
				case 1:
					return element.getSource();
				default:
					return null;
			}
		}

		@Override
		public void setData(Object value, KitFacade element, int column)
		{
		}

        @Override
		public List<? extends DataViewColumn> getDataColumns()
		{
			return columns;
		}

        @Override
		public void elementAdded(ListEvent<KitFacade> e)
		{
			kits.refilter();
		}

        @Override
		public void elementRemoved(ListEvent<KitFacade> e)
		{
			kits.refilter();
		}

        @Override
		public void elementsChanged(ListEvent<KitFacade> e)
		{
			kits.refilter();
		}

		@Override
		public void elementModified(ListEvent<KitFacade> e)
		{
			kits.refilter();
		}

        @Override
		public boolean accept(CharacterFacade context, KitFacade element)
		{
			return !element.isPermanent()
				|| !context.getKits().containsElement(element);
		}

		@Override
		public String getPrefsKey()
		{
			return isAvailModel ? "KitTreeAvail" : "KitTreeSelected";  //$NON-NLS-1$//$NON-NLS-2$
		}

	}

	private enum KitTreeView implements TreeView<KitFacade>
	{

		NAME(LanguageBundle.getString("in_nameLabel")), //$NON-NLS-1$
		TYPE_NAME(LanguageBundle.getString("in_typeName")), //$NON-NLS-1$
		SOURCE_NAME(LanguageBundle.getString("in_sourceName")); //$NON-NLS-1$
		private final String name;

		private KitTreeView(String name)
		{
			this.name = name;
		}

        @Override
		public String getViewName()
		{
			return name;
		}

		@SuppressWarnings("unchecked")
        @Override
		public List<TreeViewPath<KitFacade>> getPaths(KitFacade pobj)
		{
			switch (this)
			{
				case NAME:
					return Collections.singletonList(new TreeViewPath<>(pobj));
				case TYPE_NAME:
					TreeViewPath<KitFacade> path =
							createTreeViewPath(pobj, (Object[]) pobj
								.getDisplayType().split("\\.")); //$NON-NLS-1$
					return Arrays.asList(path);
				case SOURCE_NAME:
					return Collections.singletonList(new TreeViewPath<>(pobj, pobj.getSource()));
				default:
					throw new InternalError();
			}
		}

		/**
		 * Create a TreeViewPath for the kit and paths. 
		 * @param pobj The skill
		 * @param path The paths under which the kit should be shown.
		 * @return The TreeViewPath.
		 */
		private static TreeViewPath<KitFacade> createTreeViewPath(KitFacade pobj,
		                                                          Object... path)
		{
			if (path.length == 0)
			{
				return new TreeViewPath<>(pobj);
			}
			return new TreeViewPath<>(pobj, path);
		}

	}

}
