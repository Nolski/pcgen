/*
 * GMGenMessageHandler.java
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
 * Created on 21/07/2012 3:25:32 PM
 *
 * $Id$
 */
package pcgen.gui2.facade;

import gmgen.plugin.InitHolder;
import gmgen.plugin.InitHolderList;
import gmgen.plugin.PcgCombatant;
import gmgen.pluginmgr.messages.FileMenuSaveMessage;

import java.io.File;
import java.util.Iterator;

import pcgen.core.Globals;
import pcgen.core.PlayerCharacter;
import pcgen.facade.core.CharacterFacade;
import pcgen.gui2.PCGenFrame;
import pcgen.io.PCGFile;
import pcgen.pluginmgr.PCGenMessage;
import pcgen.pluginmgr.PCGenMessageHandler;
import pcgen.pluginmgr.messages.FocusOrStateChangeOccurredMessage;
import pcgen.pluginmgr.messages.PlayerCharacterWasLoadedMessage;
import pcgen.pluginmgr.messages.RequestFileOpenedMessageForCurrentlyOpenedPCsMessage;
import pcgen.pluginmgr.messages.RequestOpenPlayerCharacterMessage;
import pcgen.pluginmgr.messages.RequestToSavePlayerCharacterMessage;
import pcgen.pluginmgr.messages.TransmitInitiativeValuesBetweenComponentsMessage;
import pcgen.system.CharacterManager;

/**
 * The Class <code>GMGenMessageHandler</code> processes any requests 
 * to the main PCGen program from the GMGen bus.
 *
 * <br>
 * 
 * @author James Dempsey &lt;jdempsey@users.sourceforge.net&gt;
 */
public class GMGenMessageHandler implements PCGenMessageHandler
{

	private final PCGenFrame delegate;
	private final PCGenMessageHandler messageHandler;
	
	/**
	 * Create a new instance of GMGenMessageHandler
	 * @param delegate The PCGenFrame instance containing the UI.
	 */
	public GMGenMessageHandler(PCGenFrame delegate, PCGenMessageHandler mh)
	{
		this.delegate = delegate;
		this.messageHandler = mh;
	}
	
	
	@Override
	public void handleMessage(PCGenMessage message)
	{
		if (message instanceof RequestOpenPlayerCharacterMessage)
		{
			handleOpenPCGRequestMessage((RequestOpenPlayerCharacterMessage) message);
		}
		else if (message instanceof FileMenuSaveMessage)
		{
			handleSaveMessage((FileMenuSaveMessage) message);
		}
		else if (message instanceof RequestFileOpenedMessageForCurrentlyOpenedPCsMessage)
		{
			handleFetchOpenPCGRequestMessage();
		}
		else if (message instanceof RequestToSavePlayerCharacterMessage)
		{
			handleSavePcgMessage(message);
		}

		// This should only be used until GMGen can use PCGen to generate it's
		// Random encounter beasties.
		else if (message instanceof TransmitInitiativeValuesBetweenComponentsMessage)
		{
			handleInitHolderListSendMessage((TransmitInitiativeValuesBetweenComponentsMessage) message);
		}
		else if (message instanceof FocusOrStateChangeOccurredMessage)
		{
			handleStateChangedMessage();
		}
	}


	private void handleSavePcgMessage(PCGenMessage message)
	{
		RequestToSavePlayerCharacterMessage smessage = (RequestToSavePlayerCharacterMessage) message;
		PlayerCharacter pc = smessage.getPc();
		for (Iterator<CharacterFacade> iterator = CharacterManager.getCharacters().iterator(); iterator.hasNext();)
		{
			CharacterFacade facade = iterator.next();
			if (facade.matchesCharacter(pc))
			{
				CharacterManager.saveCharacter(facade);
				break;
			}
		}
	}

	private void handleSaveMessage(FileMenuSaveMessage message)
	{
//		final int currTab = baseTabbedPane.getSelectedIndex();
//		if (this.isFocused() && currTab >= FIRST_CHAR_TAB)
//		{
//			// seize the focus to cause focus listeners to fire
//			pcgenMenuBar.saveItem.requestFocus();
//
//			final PlayerCharacter aPC = getCurrentPC();
//
//			if (aPC == null)
//			{
//				return;
//			}
//
//			savePC(aPC, false);
//			message.veto();
//		}
	}

	private void handleInitHolderListSendMessage(
		TransmitInitiativeValuesBetweenComponentsMessage message)
	{
		InitHolderList list = message.getInitHolderList();

		for (int i = 0; i < list.size(); i++)
		{
			InitHolder iH = list.get(i);

			if (iH instanceof PcgCombatant)
			{
				//TODO: Resolve against the current PC list and add any new characters.
				PcgCombatant pcg = (PcgCombatant) iH;
				PlayerCharacter aPC = pcg.getPC();
				Globals.getPCList().add(aPC);
				aPC.setDirty(true);
//				addPCTab(aPC);
			}
		}
	}

	private void handleOpenPCGRequestMessage(RequestOpenPlayerCharacterMessage message)
	{
		File pcFile = message.getFile();

		if (PCGFile.isPCGenCharacterFile(pcFile))
		{
			PlayerCharacter playerCharacter =
					CharacterManager.openPlayerCharacter(pcFile, delegate,
						delegate.getLoadedDataSetRef().get(),
						message.isBlockLoadedMessage());
			message.setPlayerCharacter(playerCharacter);
			message.consume();
		}
		else if (PCGFile.isPCGenPartyFile(pcFile))
		{
			CharacterManager.openParty(pcFile, delegate, delegate
				.getLoadedDataSetRef().get());
		}
	}

	private void handleFetchOpenPCGRequestMessage()
	{
		for (int i = 0; i < CharacterManager.getCharacters().getSize(); i++)
		{
			CharacterFacade facade = CharacterManager.getCharacters().getElementAt(i);
			if (facade instanceof CharacterFacadeImpl)
			{
				CharacterFacadeImpl cfi = (CharacterFacadeImpl) facade;
				messageHandler.handleMessage(new PlayerCharacterWasLoadedMessage(this, cfi.getTheCharacter()));
			}
		}
	}

	private void handleStateChangedMessage()
	{
		// Need to fully refresh the currently displayed character
		
//		if (this.isFocused() && characterPane != null)
//		{
//			PlayerCharacter aPC =
//					getPCForTabAt(baseTabbedPane.getSelectedIndex());
//			Globals.setCurrentPC(aPC);
//			// What could possibly have changed on focus that would
//			// require a forceUpdate of all the panels?
//			// JSC -- 03/27/2004
//			//
//			// The answer to this question is this: GMGen can update characters, for
//			// example updating the experience.  This message is only ever really
//			// called when the user switches from gmgen to pcgen - and it needs to be
//			// called to ensure that all pcgen screens are updated based on any
//			// changes to the PlayerCharacter object.  Without this, what the user
//			// sees on the screen is stale.
//			// DJ -- 05/23/2004
//			characterPane
//				.setPaneForUpdate(characterPane.infoSpecialAbilities());
//			characterPane.setPaneForUpdate(characterPane.infoSummary());
//			characterPane.setPaneForUpdate(characterPane.infoRace());
//			characterPane.setPaneForUpdate(characterPane.infoClasses());
//			characterPane.setPaneForUpdate(characterPane.infoDomain());
//			//			characterPane.setPaneForUpdate(characterPane.infoFeats());
//			characterPane.setPaneForUpdate(characterPane.infoAbilities());
//			characterPane.setPaneForUpdate(characterPane.infoSkills());
//			characterPane.setPaneForUpdate(characterPane.infoSpells());
//			characterPane.setPaneForUpdate(characterPane.infoInventory());
//			characterPane.setPaneForUpdate(characterPane.infoDesc());
//			characterPane.refresh();
//			forceUpdate_PlayerTabs();
//		}
	}
}
