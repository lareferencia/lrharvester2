/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.lareferencia.xoai.services.impl.xoai;

import com.lyncode.xoai.dataprovider.core.DeleteMethod;
import com.lyncode.xoai.dataprovider.core.Granularity;
import com.lyncode.xoai.dataprovider.services.api.RepositoryConfiguration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.lareferencia.xoai.Context;
import org.lareferencia.xoai.exceptions.InvalidMetadataFieldException;
import org.lareferencia.xoai.services.api.config.ConfigurationService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author Lyncode Development Team <dspace@lyncode.com>
 * @author Domingo Iglesias <diglesias@ub.edu>
 */
public class LRRepositoryConfiguration implements RepositoryConfiguration
{
    private static Logger log = LogManager.getLogger(LRRepositoryConfiguration.class);

    private List<String> emails = null;
    private String name = null;
    private String baseUrl = null;
    private Context context;

    private ConfigurationService configurationService;

    public LRRepositoryConfiguration( ConfigurationService configurationService, Context context)
    {
        this.configurationService = configurationService;
        this.context = context;
    }

    @Override
    public List<String> getAdminEmails()
    {
        if (emails == null)
        {
            emails = new ArrayList<String>();
            String result = configurationService.getProperty("mail.admin");
            if (result == null)
            {
                log.warn("{ OAI 2.0 :: DSpace } Not able to retrieve the mail.admin property from the configuration file");
            }
            else
                emails.add(result);
        }
        return emails;
    }

    @Override
    public String getBaseUrl()
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (baseUrl == null)
        {
            baseUrl = request.getRequestURL().toString()
                    .replace(request.getPathInfo(), "");
        }
        return baseUrl + request.getPathInfo();
    }

    @Override
    public DeleteMethod getDeleteMethod()
    {
        return DeleteMethod.PERSISTENT;
    }

    @Override
    public Date getEarliestDate()
    {
        // Look at the database!
//        try
//        {
//            return dateResolver.getEarliestDate(context);
//        }
//        catch (SQLException e)
//        {
//            log.error(e.getMessage(), e);
//        }
//        catch (InvalidMetadataFieldException e)
//        {
//            log.error(e.getMessage(), e);
//        }
    	
    	// FIXME: Tal vez lo más prolijo sea mirar el indice solr?
    	
        return new Date();
    }

    @Override
    public Granularity getGranularity()
    {
        return Granularity.Second;
    }

    @Override
    public String getRepositoryName()
    {
        if (name == null)
        {
            name = configurationService.getProperty("repository.name");
            if (name == null)
            {
                log.warn("{ OAI 2.0 :: DSpace } Not able to retrieve the dspace.name property from the configuration file");
                name = "OAI Repository";
            }
        }
        return name;
    }

	@Override
	public List<String> getDescription() {
		List<String> result = new ArrayList<String>();
		String descriptionFile = configurationService.getProperty("description.file");
		//String descriptionFile = configurationService.getProperty( "description.file");

		if (descriptionFile == null) {
			// Try indexed
			boolean stop = false;
			List<String> descriptionFiles = new ArrayList<String>();
			for (int i=0;!stop;i++) {
				//String tmp = configurationService.getProperty( "description.file."+i);
				String tmp = configurationService.getProperty("description.file."+i);

				if (tmp == null) stop = true;
				else descriptionFiles.add(tmp);
			}
			
			for (String path : descriptionFiles) {
				try {
				    File f = new File(path);
				    if (f.exists())
				        result.add(FileUtils.readFileToString(f));
				} catch (IOException e) {
					log.debug(e.getMessage(), e);
				}
			}
			
		} else {
			try {
			    File f = new File(descriptionFile);
			    if (f.exists())
			        result.add(FileUtils.readFileToString(f));
			} catch (IOException e) {
				log.debug(e.getMessage(), e);
			}
		}
		return result;
	}

}
