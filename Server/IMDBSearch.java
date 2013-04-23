import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.json.*;

public class IMDBSearch extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		String inputTitle = new String(request.getParameter("title").getBytes("ISO-8859-1"), "UTF-8");
		String type = request.getParameter("title_type");
		
		JSONObject jsonResults = new JSONObject();
		JSONObject jsonResult = new JSONObject();
		
		inputTitle = URLEncoder.encode(inputTitle, "UTF-8");
		String urlString = "http://cs-server.usc.edu:12690/cgi-bin/IMDBSearch.pl?title=" + inputTitle + "&title_type=" + type;
		
		try {
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setAllowUserInteraction(false);
			InputStream urlStream = url.openStream();
			
			SAXBuilder builder = new SAXBuilder(false);
			Document doc = builder.build(urlStream);
			Element rsp = doc.getRootElement();
			String stat = rsp.getAttributeValue("stat");
			int count = 0;
			if (stat.equals("ok")) {
				Element results = rsp.getChild("results");
				String total = results.getAttributeValue("total");
				count = Integer.parseInt(total);
				if (count ==0) {
					jsonResult.put("result", 0);
				}
				else {
					List resultList = results.getChildren("result");
					JSONArray jsonArr = new JSONArray();
					Iterator iter = resultList.iterator();
					while (iter.hasNext()) {
						JSONObject json = new JSONObject();
						Element result = (Element) iter.next();
						String cover = result.getAttributeValue("cover");
						json.put("cover", cover);
						String title = result.getAttributeValue("title");
						json.put("title", title);
						String year = result.getAttributeValue("year");
						json.put("year", year);
						String director = result.getAttributeValue("director");
						json.put("director", director);
						String rating = result.getAttributeValue("rating");
						json.put("rating", rating);
						String details = result.getAttributeValue("details");
						json.put("details", details);
						jsonArr.put(json);
					}
					jsonResult.put("result", jsonArr);
				}
				jsonResults.put("results", jsonResult);
			}
			else {
				JSONObject json = new JSONObject();
				json.put("Error", "There was an error processing this request");
				jsonResult.put("result", json);
				jsonResults.put("results", jsonResult);
			}
		}
		catch (JDOMException e1) {
			JSONObject json = new JSONObject();
			json.put("Exception", e1);
			jsonResult.put("result", json);
			jsonResults.put("results", jsonResult);
		}
		catch (IOException e2) {
			JSONObject json = new JSONObject();
			json.put("Exception", e2);
			jsonResult.put("result", json);
			jsonResults.put("results", jsonResult);
		}
		catch (Exception e) {
			JSONObject json = new JSONObject();
			json.put("Exception", e);
			jsonResult.put("result", json);
			jsonResults.put("results", jsonResult);
		}
		String jsonData = jsonResults.toString();
		out.println(jsonData);		
	}
}