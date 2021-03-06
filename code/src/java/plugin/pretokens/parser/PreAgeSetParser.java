/*
 * PreAgeSetParser.java
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.       See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on December 30, 2006
 *
 * Current Ver: $Revision$
 *
 */
package plugin.pretokens.parser;

import pcgen.core.prereq.Prerequisite;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.prereq.AbstractPrerequisiteListParser;
import pcgen.persistence.lst.prereq.PrerequisiteParserInterface;
import pcgen.util.Logging;

/**
 * A prerequisite parser class that handles the parsing of pre age set tokens.
 *
 */
public class PreAgeSetParser extends AbstractPrerequisiteListParser
implements PrerequisiteParserInterface
{

	/**
	 * Get the type of prerequisite handled by this token.
	 * @return the type of prerequisite handled by this token.
	 */
    @Override
	public String[] kindsHandled() {
		return new String[]{"AGESET", "AGESETEQ", "AGESETGT", "AGESETGTEQ", "AGESETLT", 
				"AGESETLTEQ", "AGESETNEQ"}; //$NON-NLS-1$
	}

	/**
	 * Parse the pre req list
	 *
	 * @param kind The kind of the prerequisite (less the "PRE" prefix)
	 * @param formula The body of the prerequisite.
	 * @param invertResult Whether the prerequisite should invert the result.
	 * @param overrideQualify
	 *           if set true, this prerequisite will be enforced in spite
	 *           of any "QUALIFY" tag that may be present.
	 * @return PreReq
	 * @throws PersistenceLayerException
	 */
	@Override
	public Prerequisite parse(String kind,
	                          String formula,
	                          boolean invertResult,
	                          boolean overrideQualify) throws PersistenceLayerException
	{
		
		Prerequisite prereq = super.parse(kind, formula, invertResult, overrideQualify);

		//Operand should be either an integer or a recognizable String
		try{
			Integer.parseInt(prereq.getKey());
			Logging.errorPrint("You are using an old form of PREAGESET,"
					+ " you should use the age set name, e.g.: PREAGESET:1,Old");
		}
		catch (NumberFormatException exc){
			//prereq.setOperand(formula); //assume recognizable String for now
		}

		return prereq;
	
	}

}
