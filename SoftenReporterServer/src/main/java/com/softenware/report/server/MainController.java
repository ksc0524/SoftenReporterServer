package com.softenware.report.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.softenware.report.DataType;
import com.softenware.report.JsonUtils;
import com.softenware.report.KsonUtils;
import com.softenware.report.SoftenPPTXReporter;

@RestController
public class MainController {
	
//	@Autowired
//	private HttpServletRequest req;

	@RequestMapping(value="report", method=RequestMethod.POST)
	public void report(//HttpServletRequest req, HttpServletResponse res
			InputStream reqIn, OutputStream out
			, @RequestParam("temp") String template
			, @RequestParam(name="data", required=false) DataType dataType
			, @RequestParam(name="date", required=false) String defaultDateFormat
			, @RequestParam(name="out", required=false) String output
			) throws IOException, ClassNotFoundException {
		
//		String template = req.getParameter("temp");
//		DataType dataType = null;
//		if (!StringUtils.isEmpty(req.getParameter("data")))
//			dataType = DataType.valueOf(req.getParameter("data"));
//		String defaultDateFormat = req.getParameter("date");
//		String out = req.getParameter("out");
		
		Map<String, Object> data = null;
		
		if (dataType == DataType.kson) {
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new InputStreamReader(reqIn));
				
				@SuppressWarnings("unchecked")
				Map<String, Object> obj = (Map<String, Object>) KsonUtils.toObject(in);
				data = obj;
			} finally {
				if (in != null)
					in.close();
			}
		} else if (dataType == DataType.json) {
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new InputStreamReader(reqIn));
				
				@SuppressWarnings("unchecked")
				Map<String, Object> obj = (Map<String, Object>) JsonUtils.toObject(in);
				data = obj;
			} finally {
				if (in != null)
					in.close();
			}
		} else {
			ObjectInputStream in = null;
			
			try {
				in = new ObjectInputStream(reqIn);
//				in = req.getInputStream();
				
				@SuppressWarnings("unchecked")
				Map<String, Object> obj = (Map<String, Object>) in.readObject();
				data = obj;
//				int i;
//				while ((i = in.read()) > 0) {
//					System.out.println(i);
//				}
			} finally {
				if (in != null)
					in.close();
			}
		}
		
		File tempFile = new File(template);
		
		if (tempFile.exists()) {
			SoftenPPTXReporter reporter = new SoftenPPTXReporter(tempFile, data);
			
			if (!StringUtils.isEmpty(defaultDateFormat))
				reporter.setDefaultDateFormat(new SimpleDateFormat(defaultDateFormat));
			
			if (StringUtils.isEmpty(output))
				reporter.report(out);
			else
				reporter.report(new File(output));
		} else {
			throw new RuntimeException("Template file not found: " + template);
		}
	}
}
