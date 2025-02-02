/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.lareferencia.xoai.solr.exceptions;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 */
@SuppressWarnings("serial")
public class LRIndexerException extends Exception
{
    /**
     * Creates a new instance of <code>DSpaceSolrException</code> without detail
     * message.
     */
    public LRIndexerException()
    {
    }

    /**
     * Constructs an instance of <code>DSpaceSolrException</code> with the
     * specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public LRIndexerException(String msg)
    {
        super(msg);
    }

    public LRIndexerException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
