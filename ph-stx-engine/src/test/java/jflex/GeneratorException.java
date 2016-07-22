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
 *  are Copyright (C) 2016 Philip Helger
 *  All Rights Reserved.
 */
package jflex;

/**
 * Thrown when code generation has to be aborted.
 *
 * @author Gerwin Klein
 * @version JFlex 1.6.1
 */
public class GeneratorException extends RuntimeException
{

  /**
   * 
   */
  private static final long serialVersionUID = -9128247888544263982L;

  public GeneratorException ()
  {
    super ("Generation aborted");
  }

}
