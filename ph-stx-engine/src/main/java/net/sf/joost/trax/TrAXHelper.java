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
package net.sf.joost.trax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import net.sf.joost.CSTX;
import net.sf.joost.emitter.AbstractStreamEmitter;
import net.sf.joost.emitter.DOMEmitter;
import net.sf.joost.emitter.IStxEmitter;
import net.sf.joost.emitter.SAXEmitter;
import net.sf.joost.stx.Processor;

/**
 * This class provides TrAX helper functions
 *
 * @author Anatolij Zubow, Oliver Becker
 */
public class TrAXHelper
{
  private static final Logger log = LoggerFactory.getLogger (TrAXHelper.class);

  /**
   * Defaultconstructor
   */
  protected TrAXHelper ()
  {}

  /**
   * Helpermethod for getting an InputSource from a StreamSource.
   *
   * @param source
   *        <code>Source</code>
   * @return An <code>InputSource</code> object or null
   * @throws TransformerConfigurationException
   */
  protected static InputSource getInputSourceForStreamSources (final Source source,
                                                               final ErrorListener errorListener) throws TransformerConfigurationException
  {

    if (CSTX.DEBUG)
      log.debug ("getting an InputSource from a StreamSource");
    InputSource input = null;
    String systemId = source.getSystemId ();

    if (systemId == null)
    {
      systemId = "";
    }
    try
    {
      if (source instanceof StreamSource)
      {
        if (CSTX.DEBUG)
          log.debug ("Source is a StreamSource");
        final StreamSource stream = (StreamSource) source;
        final InputStream istream = stream.getInputStream ();
        final Reader reader = stream.getReader ();
        // Create InputSource from Reader or InputStream in Source
        if (istream != null)
        {
          input = new InputSource (istream);
        }
        else
        {
          if (reader != null)
          {
            input = new InputSource (reader);
          }
          else
          {
            input = new InputSource (systemId);
          }
        }
      }
      else
      {
        // Source type is not supported
        if (errorListener != null)
        {
          try
          {
            errorListener.fatalError (new TransformerConfigurationException ("Source is not a StreamSource"));
            return null;
          }
          catch (final TransformerException e2)
          {
            if (CSTX.DEBUG)
              log.debug ("Source is not a StreamSource");
            throw new TransformerConfigurationException ("Source is not a StreamSource");
          }
        }
        if (CSTX.DEBUG)
          log.debug ("Source is not a StreamSource");
        throw new TransformerConfigurationException ("Source is not a StreamSource");
      }
      // setting systemId
      input.setSystemId (systemId);
      // } catch (NullPointerException nE) {
      // //catching NullPointerException
      // if(errorListener != null) {
      // try {
      // errorListener.fatalError(
      // new TransformerConfigurationException(nE));
      // return null;
      // } catch( TransformerException e2) {
      // log.debug(nE);
      // throw new TransformerConfigurationException(nE.getMessage());
      // }
      // }
      // log.debug(nE);
      // throw new TransformerConfigurationException(nE.getMessage());
    }
    catch (final SecurityException sE)
    {
      // catching SecurityException
      if (errorListener != null)
      {
        try
        {
          errorListener.fatalError (new TransformerConfigurationException (sE));
          return null;
        }
        catch (final TransformerException e2)
        {
          if (CSTX.DEBUG)
            log.debug ("Exception", sE);
          throw new TransformerConfigurationException (sE.getMessage ());
        }
      }
      if (CSTX.DEBUG)
        log.debug ("Exception", sE);
      throw new TransformerConfigurationException (sE.getMessage ());
    }
    return (input);
  }

  /**
   * HelperMethod for initiating StxEmitter.
   *
   * @param result
   *        A <code>Result</code> object.
   * @param processor
   * @param aOutputProperties
   * @return An {@link IStxEmitter}
   * @throws javax.xml.transform.TransformerException
   */
  public static IStxEmitter initStxEmitter (final Result result,
                                            final Processor processor,
                                            final Properties aOutputProperties) throws TransformerException
  {
    Properties outputProperties = aOutputProperties;
    if (outputProperties == null)
      outputProperties = processor.m_aOutputProperties;

    if (CSTX.DEBUG)
      log.debug ("init StxEmitter");
    // Return the content handler for this Result object
    try
    {
      // Result object could be SAXResult, DOMResult, or StreamResult
      if (result instanceof SAXResult)
      {
        final SAXResult target = (SAXResult) result;
        final ContentHandler handler = target.getHandler ();
        if (handler != null)
        {
          if (CSTX.DEBUG)
            log.debug ("return SAX specific Implementation for " + "StxEmitter");
          // SAX specific Implementation
          return new SAXEmitter (handler);
        }
      }
      else
        if (result instanceof DOMResult)
        {
          if (CSTX.DEBUG)
            log.debug ("return DOM specific Implementation for " + "StxEmitter");
          // DOM specific Implementation
          return new DOMEmitter ((DOMResult) result);
        }
        else
          if (result instanceof StreamResult)
          {
            if (CSTX.DEBUG)
              log.debug ("return StreamResult specific Implementation " + "for StxEmitter");
            // Get StreamResult
            final StreamResult target = (StreamResult) result;
            // StreamResult may have been created with a java.io.File,
            // java.io.Writer, java.io.OutputStream or just a String
            // systemId.
            // try to get a Writer from Result object
            final Writer writer = target.getWriter ();
            if (writer != null)
            {
              if (CSTX.DEBUG)
                log.debug ("get a Writer object from Result object");
              return AbstractStreamEmitter.newEmitter (writer, CSTX.DEFAULT_ENCODING, outputProperties);
            }
            // or try to get an OutputStream from Result object
            final OutputStream ostream = target.getOutputStream ();
            if (ostream != null)
            {
              if (CSTX.DEBUG)
                log.debug ("get an OutputStream from Result object");
              return AbstractStreamEmitter.newEmitter (ostream, outputProperties);
            }
            // or try to get just a systemId string from Result object
            final String systemId = result.getSystemId ();
            if (CSTX.DEBUG)
              log.debug ("get a systemId string from Result object");
            if (systemId == null)
            {
              if (CSTX.DEBUG)
                log.debug ("JAXP_NO_RESULT_ERR");
              throw new TransformerException ("JAXP_NO_RESULT_ERR");
            }
            // System Id may be in one of several forms, (1) a uri
            // that starts with 'file:', (2) uri that starts with 'http:'
            // or (3) just a filename on the local system.
            OutputStream os = null;
            URL url = null;
            if (systemId.startsWith ("file:"))
            {
              url = new URL (systemId);
              os = new FileOutputStream (url.getFile ());
              return AbstractStreamEmitter.newEmitter (os, outputProperties);
            }
            else
              if (systemId.startsWith ("http:"))
              {
                url = new URL (systemId);
                final URLConnection connection = url.openConnection ();
                os = connection.getOutputStream ();
                return AbstractStreamEmitter.newEmitter (os, outputProperties);
              }
              else
              {
                // system id is just a filename
                final File tmp = new File (systemId);
                url = tmp.toURI ().toURL ();
                os = new FileOutputStream (url.getFile ());
                return AbstractStreamEmitter.newEmitter (os, outputProperties);
              }
          }
      // If we cannot create the file specified by the SystemId
    }
    catch (final IOException iE)
    {
      if (CSTX.DEBUG)
        log.debug ("Exception", iE);
      throw new TransformerException (iE);
    }
    catch (final ParserConfigurationException pE)
    {
      if (CSTX.DEBUG)
        log.debug ("Exception", pE);
      throw new TransformerException (pE);
    }
    return null;
  }

  /**
   * Converts a supplied <code>Source</code> to a <code>SAXSource</code>.
   *
   * @param source
   *        The supplied input source
   * @param errorListener
   *        an ErrorListener object
   * @return a <code>SAXSource</code>
   */
  public static SAXSource getSAXSource (final Source source,
                                        final ErrorListener errorListener) throws TransformerException
  {

    if (CSTX.DEBUG)
      log.debug ("getting a SAXSource from a Source");
    // SAXSource
    if (source instanceof SAXSource)
    {
      if (CSTX.DEBUG)
        log.debug ("source is an instance of SAXSource, so simple return");
      return (SAXSource) source;
    }
    // DOMSource
    if (source instanceof DOMSource)
    {
      if (CSTX.DEBUG)
        log.debug ("source is an instance of DOMSource");
      final InputSource is = new InputSource ();
      final Node startNode = ((DOMSource) source).getNode ();
      Document doc;
      if (startNode instanceof Document)
      {
        doc = (Document) startNode;
      }
      else
      {
        doc = startNode.getOwnerDocument ();
      }
      if (CSTX.DEBUG)
        log.debug ("using DOMDriver");
      final DOMDriver driver = new DOMDriver ();
      driver.setDocument (doc);
      is.setSystemId (source.getSystemId ());
      driver.setSystemId (source.getSystemId ());
      return new SAXSource (driver, is);
    }
    // StreamSource
    if (source instanceof StreamSource)
    {
      if (CSTX.DEBUG)
        log.debug ("source is an instance of StreamSource");
      final InputSource isource = getInputSourceForStreamSources (source, errorListener);
      return new SAXSource (isource);
    }

    final String errMsg = "Unknown type of source";
    log.error (errMsg);
    final IllegalArgumentException iE = new IllegalArgumentException (errMsg);
    final TransformerConfigurationException tE = new TransformerConfigurationException (iE.getMessage (), iE);
    if (errorListener != null)
      errorListener.error (tE);
    else
      throw tE;
    return null;
  }
}
