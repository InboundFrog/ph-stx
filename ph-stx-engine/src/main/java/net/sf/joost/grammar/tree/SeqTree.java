/**
 *  The contents of this file are subject to the Mozilla Public License
 *  Version 1.1 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is: this file
 *
 *  The Initial Developer of the Original Code is Oliver Becker.
 *
 *  Portions created by Philip Helger
 *  are Copyright (C) 2016-2017 Philip Helger
 *  All Rights Reserved.
 */
package net.sf.joost.grammar.tree;

import org.xml.sax.SAXException;

import net.sf.joost.grammar.AbstractTree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

/**
 * Objects of SeqTree represent nodes of a sequence in the syntax tree of a
 * pattern or an STXPath expression.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/09/29 05:59:51 $
 * @author Oliver Becker
 */
public final class SeqTree extends AbstractTree
{
  public SeqTree ()
  {
    super (SEQ);
  }

  public SeqTree (final AbstractTree left, final AbstractTree right)
  {
    super (SEQ, left, right);
  }

  @Override
  public Value evaluate (final Context context, final int top) throws SAXException
  {
    Value v1, v2;
    if (m_aLeft != null)
      v1 = m_aLeft.evaluate (context, top);
    else
      v1 = Value.VAL_EMPTY;
    if (m_aRight != null)
      v2 = m_aRight.evaluate (context, top);
    else
      v2 = Value.VAL_EMPTY;

    // if we got an empty sequence, return the other value
    if (v1.type == Value.EMPTY)
      return v2;
    if (v2.type == Value.EMPTY)
      return v1;

    return Value.concat (v1, v2);
  }
}
