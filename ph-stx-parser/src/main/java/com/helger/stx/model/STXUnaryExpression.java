/**
 * Copyright (C) 2016-2017 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.stx.model;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.string.ToStringGenerator;

public class STXUnaryExpression extends AbstractSTXExpression
{
  private final ESTXOperator m_eOperator;
  private final ISTXExpression m_aExpression;

  public STXUnaryExpression (@Nonnull final ESTXOperator eOperator, @Nonnull final ISTXExpression aExpression)
  {
    m_eOperator = ValueEnforcer.notNull (eOperator, "Operator");
    m_aExpression = ValueEnforcer.notNull (aExpression, "Expression");
  }

  @Nonnull
  public ESTXOperator getOperator ()
  {
    return m_eOperator;
  }

  @Nonnull
  public ISTXExpression getExpression ()
  {
    return m_aExpression;
  }

  public void writeTo (@Nonnull final Writer aWriter) throws IOException
  {
    m_eOperator.writeTo (aWriter);
    m_aExpression.writeTo (aWriter);
  }

  @Override
  @Nonnull
  public String toString ()
  {
    return new ToStringGenerator (this).append ("operator", m_eOperator)
                                       .append ("expression", m_aExpression)
                                       .getToString ();
  }
}
