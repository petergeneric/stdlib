package com.peterphi.carbon.type.mutable;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public class CarbonDestination extends CarbonModule
{
	/**
	 * Filter_0 is for video filters
	 */
	private static final String VIDEO_FILTER_CONTAINER = "Filter_0";

	/**
	 * PostconversionTasks is for post conversion tasks
	 * <p/>
	 * eg FTPUpload, FileDelete
	 */
	private static final String POST_CONVERSION_TASKS_CONTAINER = "PostconversionTasks";

	private static final String POST_CONVERSION_FILE_DELETE = "FileDelete";

	public CarbonDestination()
	{
		this(new Element("Module_XXXX"));
	}

	public CarbonDestination(Element element)
	{
		super(element);
	}

	//
	// Attribute helpers
	//

	public void setDestinationUNC(String value)
	{
		getOrCreateModuleData().setAttribute("FullUNCPath", value);
	}

	public String getDestinationUNC()
	{
		return getOrCreateModuleData().getAttribute("FullUNCPath");
	}

	public void setDestinationBaseFilename(String value)
	{
		getOrCreateModuleData().setAttribute("CML_P_BaseFileName", value);
	}

	public String getDestinationBaseFilename()
	{
		return getOrCreateModuleData().getAttribute("CML_P_BaseFileName");
	}

	//
	// Video filters
	//

	public void addVideoFilter(CarbonModule module)
	{
		final Element filterContainer = getOrCreateVideoFilterElement();

		// We should use lastModule number + 1 as the number for the new module
		final int moduleNumber = filterContainer.getChildren().size();


		final Element moduleElement = module.getElement();
		moduleElement.detach();

		moduleElement.setName("Module_" + moduleNumber);
		filterContainer.addContent(moduleElement);
	}

	private Element getVideoFilterElement()
	{
		return element.getChild(VIDEO_FILTER_CONTAINER);
	}

	private Element getOrCreateVideoFilterElement()
	{
		if (getVideoFilterElement() == null)
			element.addContent(new Element(VIDEO_FILTER_CONTAINER));

		return getVideoFilterElement();
	}

	public List<CarbonModule> getVideoFilters()
	{
		List<CarbonModule> filters = new ArrayList<CarbonModule>();

		final Element filterContainer = getVideoFilterElement();

		if (filterContainer != null)
		{
			for (Element element : filterContainer.getChildren())
			{
				filters.add(new CarbonModule(element));
			}
		}


		return filters;
	}

	//
	// Post Transcode FTP Upload
	//
	public void addFTPUpload(CarbonFTPUpload ftpUpload)
	{

		final Element postConversionTasksContainer = getOrCreatePostConversionTasksElement();

		final int moduleNumber = getPostConversionFTPUploads().size();

		final Element ftpUploadElement = ftpUpload.getElement();
		ftpUploadElement.detach();

		ftpUploadElement.setName(CarbonFTPUpload.FTP_UPLOAD_ELEMENT_NAME_PREFIX + moduleNumber);
		postConversionTasksContainer.addContent(ftpUploadElement);

	}

	public List<CarbonFTPUpload> getPostConversionFTPUploads()
	{
		List<CarbonFTPUpload> uploads = new ArrayList<CarbonFTPUpload>();

		final Element postConversionTasksContainer = getPostConversionTasksElement();

		if (postConversionTasksContainer != null)
		{
			for (Element element : postConversionTasksContainer.getChildren())
			{
				//PostconversionTasks may contain other elements, not just FTPUploads				
				if (element.getName() != null && element.getName().startsWith(CarbonFTPUpload.FTP_UPLOAD_ELEMENT_NAME_PREFIX))
				{
					uploads.add(new CarbonFTPUpload(element));
				}
			}
		}

		return uploads;
	}

	private Element getOrCreatePostConversionTasksElement()
	{
		if (getPostConversionTasksElement() == null)
			element.addContent(new Element(POST_CONVERSION_TASKS_CONTAINER));

		return getPostConversionTasksElement();
	}

	private Element getPostConversionTasksElement()
	{
		return element.getChild(POST_CONVERSION_TASKS_CONTAINER);
	}

	// 
	// Post transcode delete
	//
	public void addPostTranscodeDelete()
	{
		Element postConversionTasks = getOrCreatePostConversionTasksElement();
		postConversionTasks.addContent(new Element(POST_CONVERSION_FILE_DELETE));
	}
}
