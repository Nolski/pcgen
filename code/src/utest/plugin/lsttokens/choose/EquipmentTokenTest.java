/*
 * 
 * Copyright (c) 2010 Tom Parker <thpr@users.sourceforge.net>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package plugin.lsttokens.choose;


import org.junit.Test;

import pcgen.cdom.base.CDOMObject;
import pcgen.core.Equipment;
import pcgen.core.Race;
import pcgen.core.WeaponProf;
import pcgen.rules.persistence.CDOMLoader;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.rules.persistence.token.CDOMSecondaryToken;
import pcgen.rules.persistence.token.QualifierToken;
import plugin.lsttokens.ChooseLst;
import plugin.lsttokens.testsupport.AbstractChooseTokenTestCase;
import plugin.lsttokens.testsupport.CDOMTokenLoader;

public class EquipmentTokenTest extends
		AbstractChooseTokenTestCase<CDOMObject, Equipment>
{

	static ChooseLst token = new ChooseLst();
	static EquipmentToken subtoken = new EquipmentToken();
	static CDOMTokenLoader<CDOMObject> loader = new CDOMTokenLoader<CDOMObject>();

	@Override
	public Class<Race> getCDOMClass()
	{
		return Race.class;
	}

	@Override
	public CDOMLoader<CDOMObject> getLoader()
	{
		return loader;
	}

	@Override
	public CDOMPrimaryToken<CDOMObject> getToken()
	{
		return token;
	}

	@Override
	public CDOMSecondaryToken<?> getSubToken()
	{
		return subtoken;
	}

	@Override
	public Class<Equipment> getTargetClass()
	{
		return Equipment.class;
	}

	@Test
	public void testEmpty()
	{
		// Just to get Eclipse to recognize this as a JUnit 4.0 Test Case
	}

	@Override
	protected boolean allowsQualifier()
	{
		return true;
	}

	@Override
	protected String getChoiceTitle()
	{
		return subtoken.getDefaultTitle();
	}

	@Override
	protected QualifierToken<WeaponProf> getPCQualifier()
	{
		return null;
	}

	@Override
	protected boolean isTypeLegal()
	{
		return true;
	}

	@Override
	protected boolean isAllLegal()
	{
		return true;
	}
}
