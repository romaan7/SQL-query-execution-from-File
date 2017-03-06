/* Author: RS26812
 * Initials: RS
 * Version: 3.00
 * Team: SSE BAU/ MWM application support.
 * Last update Date: 01/03/2017
 * */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class DailyCheck {

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, ClassNotFoundException, SQLException {
		
		
		StringBuilder highlightsContainer=new StringBuilder();
		StringBuilder tableContainer=new StringBuilder();
		
		StringBuilder htmlContainer=new StringBuilder(htmlProperties());
		
		String server="";
		
		String username="";
		String password="";
		String xmlfilename="";
		
		String highlights="";
			
		String to ="";
		String[] toAddress={};
		
		String cc="";
		String[] ccAddress={};
		
		String attachment="";
		String[] attachmentFiles={};
		
		StringBuilder subject=new StringBuilder();
		StringBuilder title=new StringBuilder();
		
	    String from = "";
	    String host = ""; 
try{		
		InputParameter params = new InputParameter();
	    new JCommander(params,args);

		server=params.server;
		username=params.username;
		password=params.password;
		xmlfilename=params.inputfile;
		
		highlights=params.highlights;
		host = params.mailhost;  
		to = params.to;
		toAddress = to.split(",");
		
		cc=params.cc;
		if(cc!=null){ccAddress = cc.split(",");}
		
		attachment=params.attachment;
		if(attachment!=null){attachmentFiles=attachment.split(",");}
		
	    from = params.from;
	    if(from==null){from = "mwm.application.support@sse.com";}
	   
	    
} catch (ParameterException ex) {
    System.out.println(ex.getMessage());
    InputParameter usages = new InputParameter();
    usages.usage();}
     
     
String connurl=("jdbc:oracle:thin:@"+server);
String jdbcurl=(connurl+","+username+","+password);
		

Class.forName("oracle.jdbc.driver.OracleDriver");
Connection con = DriverManager.getConnection(connurl,username,password);

		
		Properties properties = System.getProperties();  
	    properties.setProperty("mail.smtp.host", host);  
	    
	    
	    //if(highlights!=null){highlightsContainer=generateHighlights(highlights,con);}//generates the highlight section}
	    //else{System.out.println("No highlights input file specified.Skipping the Highlights generation");}
		    
	    tableContainer=generateHtmlTable(xmlfilename,subject,title,con,htmlContainer,highlightsContainer);//generates the html table section.
	    
	    
		sendMail(highlightsContainer,tableContainer,attachmentFiles,properties,from,toAddress,ccAddress,subject,title);

}//end main


public static StringBuilder generateHtmlTable(String xmlfilename,StringBuilder subject,StringBuilder title,Connection con,StringBuilder htmlContainer,StringBuilder highlightsContainer)throws ParserConfigurationException, SQLException, FileNotFoundException, IOException, SAXException{
	
	try{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
		.newInstance();
DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
    	Document doc = docBuilder.parse(new FileInputStream(xmlfilename));
		
		// normalize text representation
		doc.getDocumentElement().normalize();
		System.out.println("Root element of the xml is "
				+ doc.getDocumentElement().getNodeName());

		subject.append(doc.getDocumentElement().getAttribute("subject"));
		title.append(doc.getDocumentElement().getAttribute("title"));

		NodeList listOfQueries = doc.getElementsByTagName("row");
		int totalQueries = listOfQueries.getLength();
		System.out.println("Total no of queries : " + totalQueries);
		
		
		NodeList listOfHighlites = doc.getElementsByTagName("highlites");
		int totalHighlites = listOfHighlites.getLength();
		System.out.println("Total no of highlites : " + totalHighlites);
		
		//try merge highlites start
		
		
		highlightsContainer.append("<ul>");
		
		for (int highlitesCount = 0; highlitesCount <totalHighlites; highlitesCount++) {
			Node xmlNode = listOfHighlites.item(highlitesCount);
			if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {

				Element xmlElement = (Element) xmlNode;

				//
				NodeList indexNodeList = xmlElement.getElementsByTagName("index");
			    Element indexElement = (Element) indexNodeList.item(0);
			   
			    
				NodeList textindexnumberNodeList = indexElement.getChildNodes();
				String index = new String(((Node) textindexnumberNodeList.item(0)).getNodeValue().trim());
				System.out.println("Index: " + index);
				
				//
				NodeList statmentsNodeList = xmlElement.getElementsByTagName("statments");
				Element statmentsElement = (Element) statmentsNodeList.item(0);
				
				NodeList textstatmentsNodeList = statmentsElement.getChildNodes();
				//StringBuilder statment = new StringBuilder(((Node) textstatmentsNodeList.item(0)).getNodeValue().trim());
				String statment = ((Node) textstatmentsNodeList.item(0)).getNodeValue().trim();
				System.out.println("Statment : " +statment);
				
				//
				NodeList queryList = xmlElement.getElementsByTagName("query");
				Element queryElement = (Element) queryList.item(0);
				
				NodeList textQueryList = queryElement.getChildNodes();
				String query = ((Node) textQueryList.item(0)).getNodeValue().trim();
				//System.out.println("Query: " +query);
				//
				NodeList checkIncidentList = xmlElement.getElementsByTagName("checkIncident");
				Element checkIncidentElement = (Element) checkIncidentList.item(0);
				
				NodeList textcheckIncidentList = checkIncidentElement.getChildNodes();
				String checkIncident = ((Node) textcheckIncidentList.item(0)).getNodeValue().trim();
				System.out.println("checkIncident: " +checkIncident);
				
				ArrayList<String> listOfResultsetValues = new ArrayList<String>();
				
				String MUM_Invocation_command="se_send_alert -severity WARNING -group \"MWMPROD - WARNING TEST AUTOMATED ALERT: DATE=$DATE\" -slot 1 \"TEST AUTOMATED ALERT -MWMPROD\"";
				
				if(checkIncident.contains("Y"))
				{
					
					try{
					Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
					ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
					int colCount=rsmd.getColumnCount();
						while (rs.next()) {
							for (int k=1;k<=colCount;k++){//FOR EVERY ROW
								String resultValue=rs.getString(k);
								listOfResultsetValues.add(resultValue);//Add the result in the array
							}				
						}
						System.out.println("VALUES : "+listOfResultsetValues.toString());
						
						//Below code invokes the command for raising an ITSC incident.
						if(!listOfResultsetValues.isEmpty()||listOfResultsetValues.size()!=0){
							String ITSCqueueName=checkIncidentElement.getAttribute("ITSCqueueName");
							//MUM_Invocation_command="se_send_alert -severity WARNING -group \" "+ ITSCqueueName +" - WARNING TEST AUTOMATED ALERT: DATE=$DATE\" -slot 1 \"" +statment+ "\"";
							MUM_Invocation_command="java -version";
							String op=executeCommand(MUM_Invocation_command);
							System.out.println("EXECUTE :"+MUM_Invocation_command+"\n"+"CMD OUTPUT"+op);
							//executeCommand(MUM_Invocation_command);
							
							for (int i=0;i<=listOfResultsetValues.size()-1;i++){
								statment=statment.toString().replaceFirst("(?:%)+", listOfResultsetValues.get(i));
							}
							highlightsContainer.append(statment+"<br>"+"<br>");
						}
						
					}catch (SQLException e ) {System.out.println(e);} 	
				}
				else 
				{
				try {
				Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
				ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
				int colCount=rsmd.getColumnCount();
					while (rs.next()) {
						for (int k=1;k<=colCount;k++){//FOR EVERY ROW
							String resultValue=rs.getString(k);
							listOfResultsetValues.add(resultValue);//Add the result in the array
						}												
					}
					System.out.println("VALUES : "+listOfResultsetValues.toString());
					} catch (SQLException e ) {System.out.println(e);} 
					
				//Replace all % in the statment with the values of listOfResultsetValues array one by one
					for (int i=0;i<=listOfResultsetValues.size()-1;i++){
						statment=statment.toString().replaceFirst("(?:%)+", listOfResultsetValues.get(i));
					}
					
					highlightsContainer.append(statment+"<br>"+"<br>");
				}
			}	
		}highlightsContainer.append("</ul>");
		
		
		
		//---try merge highlites ends
		for (int s = 0; s <totalQueries; s++) {
			
			Node xmlNode = listOfQueries.item(s);
			
			if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {

				Element xmlElement = (Element) xmlNode;

				//
				NodeList serialnumberNodeList = xmlElement.getElementsByTagName("serialNumber");
			    Element serialnumberElement = (Element) serialnumberNodeList.item(0);
			   
				NodeList textserialnumberNodeList = serialnumberElement.getChildNodes();
				String serialnumber = new String(((Node) textserialnumberNodeList.item(0)).getNodeValue().trim());
				System.out.println("SERIAL NUMBER : " + serialnumber);
				
				//
				NodeList entityNodeList = xmlElement.getElementsByTagName("entity");
				Element entityElement = (Element) entityNodeList.item(0);
				
				NodeList textEntityNodeList = entityElement.getChildNodes();
				String entity = new String(((Node) textEntityNodeList.item(0)).getNodeValue().trim());
				System.out.println("ENTITY : " +entity);
				
				//
				NodeList checkList = xmlElement.getElementsByTagName("check");
				Element checkElement = (Element) checkList.item(0);
				
				NodeList textCheckList = checkElement.getChildNodes();
				String check = ((Node) textCheckList.item(0)).getNodeValue().trim();
				System.out.println("CHECK : " + check);
				//
				NodeList descriptionList = xmlElement.getElementsByTagName("description");
				Element descriptionElement = (Element) descriptionList.item(0);
				
				NodeList textdescriptionList = descriptionElement.getChildNodes();
				String description = ((Node) textdescriptionList.item(0)).getNodeValue().trim();
				System.out.println("DESCRIPTION : " + description);

				//
				NodeList queryList = xmlElement.getElementsByTagName("query");
				Element queryElement = (Element) queryList.item(0);
				
				NodeList textQueryList = queryElement.getChildNodes();
				String query = ((Node) textQueryList.item(0)).getNodeValue().trim();
				//System.out.println("QUERY : " +query)
				
				//----------------------start executing the SQL statments from XML 
				
				try {
					Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
					ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
					int columnCount =rsmd.getColumnCount();//GET COLUMN CONT( NUMBER OF COLUMNS RETURNED BY QUERY
					int rowCount = 0;
					int firstrow=1;//SET VERIABLE FOR FORMATING THE HTML TABLE ACCORDING TO THE TABLE ROWS
					
					rs.last();
					rowCount = rs.getRow();
					rs.beforeFirst();

					htmlContainer.append("<tr><td rowspan="+rowCount+">"+entity+"</td><td rowspan="+rowCount+">"+check+"</td><td rowspan="+rowCount+">"+description+"</td>");
    
					if(!rs.next()){ //IF NO RECORDS ARE RETURNED. THIS MOVES CURSOR TO NEXT ROW
						System.out.println("No data to display");
						htmlContainer.append("<td> - </td><td> - </td></tr>");

// COMMENTING THE CODE FOR FORMATING ROWS(this was implemented before implementing the row merging
/*						if(firstrow==1){
						htmlContainer.append("<td> - </td><td> - </td></tr>");
						firstrow=0;
						}
						else{
						htmlContainer.append("<td></td>"+"<td></td>"+"<td></td>"+"<td> - </td><td> - </td></tr>");
						}
*/
					}
					else{
				for (int j=1;j<=columnCount;j++){//LOOP FOR GETTING ALL THE COLUMNS RETURNED BY THE QUERY
						rs.previous();//MOVE THE CURSOR TO PREVIOUS ROW
						while (rs.next()) {
							
							for (int k=0;k<columnCount;k++){//FOR EVERY ROW
								String column=rs.getString(rsmd.getColumnName(j+k));
								htmlContainer.append("<td>"+column+"</td>");
								
								System.out.println("OUTPUT : "+column);
							}
							htmlContainer.append("</tr>");

							
//The below commented code performed the formating of rows before the cell merging was implemented.							
/*							
							if(firstrow==1){//IF THE ROW PRINTED IS THE FIRST ROW
							for (int k=0;k<columnCount;k++){//FOR EVERY ROW
								String column=rs.getString(rsmd.getColumnName(j+k));
								htmlContainer.append("<td>");
								htmlContainer.append(column);
								
								System.out.println("OUTPUT : "+column +"\b");
							}
							htmlContainer.append("</td></tr>");
							firstrow=0;
							}
							else{
									for (int k=0;k<columnCount;k++){
										String column=rs.getString(rsmd.getColumnName(j+k));
										
										if(k==0){//PRINT 3 BLANK HTML TABLE CELLS FOR FORMATING(FOR FIRST COLUMN)
										htmlContainer.append("<td></td><td></td><td></td><td>");
										}
										else{ //FOR REST OF THE COLUMNS PRINT THE COLUMNS
										htmlContainer.append("<td>");
										}
										htmlContainer.append(column);
										System.out.println("OUTPUT : "+column);
									}
									htmlContainer.append("</td></tr>");
									}

*/
							
							
							
/*							Integer incidentrowcheck = Integer.valueOf(incidentRowNumber);
							//Integer queryColumnValue= Integer.valueOf(rs.getString(rsmd.getColumnName(j+1)));
							Integer thresholdFromIncident= Integer.valueOf(incidentThreshold);
*/
						}
						}
					}
				}catch (SQLException e ) {System.out.println(e);} 
				//------------------ End the sql execution.
			}
	}
	con.close();//CLOSE THE CONNECTOIN TO DB
	
}catch (FileNotFoundException e){
	 System.out.println("Input File Not found");}
	return htmlContainer;
	
	
}//endgenerateHTMLtable method


/*
 * Commenting this code as this has been merged with html generator and only requires one file as input.
public static StringBuilder generateHighlights(String highlightsInputFile,Connection connectoin) throws ParserConfigurationException, SQLException, FileNotFoundException, IOException{
		
		StringBuilder highlightContainer=new StringBuilder();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
		.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		
		try{
	    	Document doc = docBuilder.parse(new FileInputStream(highlightsInputFile));
			
			// normalize text representation
			doc.getDocumentElement().normalize();
			System.out.println("Root element of the Highlights xml is "
					+ doc.getDocumentElement().getNodeName());

			NodeList listOfQueries = doc.getElementsByTagName("bulletPoint");
			int totalQueries = listOfQueries.getLength();
			System.out.println("Total no of highlites : " + totalQueries);

			for (int s = 0; s <totalQueries; s++) {
				
				Node xmlNode = listOfQueries.item(s);
				
				if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {

					Element xmlElement = (Element) xmlNode;

					//
					NodeList indexNodeList = xmlElement.getElementsByTagName("index");
				    Element indexElement = (Element) indexNodeList.item(0);
				   
				    
					NodeList textindexnumberNodeList = indexElement.getChildNodes();
					String index = new String(((Node) textindexnumberNodeList.item(0)).getNodeValue().trim());
					System.out.println("Index: " + index);
					
					//
					NodeList statmentsNodeList = xmlElement.getElementsByTagName("statments");
					Element statmentsElement = (Element) statmentsNodeList.item(0);
					
					NodeList textstatmentsNodeList = statmentsElement.getChildNodes();
					//StringBuilder statment = new StringBuilder(((Node) textstatmentsNodeList.item(0)).getNodeValue().trim());
					String statment = ((Node) textstatmentsNodeList.item(0)).getNodeValue().trim();
					System.out.println("Statment : " +statment);
					
					//
					NodeList queryList = xmlElement.getElementsByTagName("query");
					Element queryElement = (Element) queryList.item(0);
					
					NodeList textQueryList = queryElement.getChildNodes();
					String query = ((Node) textQueryList.item(0)).getNodeValue().trim();
					System.out.println("Query: " +query);
					//
					NodeList checkIncidentList = xmlElement.getElementsByTagName("checkIncident");
					Element checkIncidentElement = (Element) checkIncidentList.item(0);
					
					NodeList textcheckIncidentList = checkIncidentElement.getChildNodes();
					String checkIncident = ((Node) textcheckIncidentList.item(0)).getNodeValue().trim();
					System.out.println("checkIncident: " +checkIncident);
					
					ArrayList<String> listOfResultsetValues = new ArrayList<String>();
					
					String MUM_Invocation_command="se_send_alert -severity WARNING -group \"MWMPROD - WARNING TEST AUTOMATED ALERT: DATE=$DATE\" -slot 1 \"TEST AUTOMATED ALERT -MWMPROD\"";
					
					if(checkIncident.contains("Y"))
					{
						try{
						Statement stmt = connectoin.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
						ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
						ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
						int colCount=rsmd.getColumnCount();
							while (rs.next()) {
								for (int k=1;k<=colCount;k++){//FOR EVERY ROW
									String resultValue=rs.getString(k);
									listOfResultsetValues.add(resultValue);
									System.out.println("highlights values: "+resultValue);
								}				
							}
							
							if(!listOfResultsetValues.isEmpty()||listOfResultsetValues.size()!=0){
								
								NodeList ITSCqueueNameList = xmlElement.getElementsByTagName("checkIncident");
								Element ITSCqueueNameElement = (Element) ITSCqueueNameList.item(0);
								
								NodeList textITSCqueueNameList = ITSCqueueNameElement.getChildNodes();
								String ITSCqueueName = ((Node) textITSCqueueNameList.item(0)).getNodeValue().trim();
								System.out.println("ITSCqueueName: " +ITSCqueueName);
								
								MUM_Invocation_command="se_send_alert -severity WARNING -group \" "+ ITSCqueueName +" - WARNING TEST AUTOMATED ALERT: DATE=$DATE\" -slot 1 \"" +statment+ "\"";
								System.out.println(MUM_Invocation_command);
								executeCommand(MUM_Invocation_command);
							}
							
						}catch (SQLException e ) {System.out.println(e);} 
					}
					else 
					{
					try {
					Statement stmt = connectoin.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					ResultSet rs = stmt.executeQuery(query);//EXECUTE QUERIES FROM query TAG IN XML
					ResultSetMetaData rsmd = rs.getMetaData();//GET METADATA FOR RESULTSET
					int colCount=rsmd.getColumnCount();
						while (rs.next()) {
							for (int k=1;k<=colCount;k++){//FOR EVERY ROW
								String resultValue=rs.getString(k);
								listOfResultsetValues.add(resultValue);
								System.out.println("highlights values: "+resultValue);
							}												
						} 
						} catch (SQLException e ) {System.out.println(e);} 
					
						for (int i=0;i<=listOfResultsetValues.size()-1;i++){
							statment=statment.toString().replaceFirst("(?:%)+", listOfResultsetValues.get(i));
						}
						
						highlightContainer.append("<li><ul>"+statment+"</ul></li>");
					}
				}
			}
		}catch (SAXException e) {e.printStackTrace();}
		
		return highlightContainer;
	
}//end generateHighlights method
*/	
	
public static void sendMail(StringBuilder firstPart,StringBuilder secondPart,String[] attachmentFiles,Properties properties, String from, String[] toAddress,String[] ccAddress,StringBuilder subject,StringBuilder title){
		//create session fore mail
		 Session session = Session.getDefaultInstance(properties);  
		  
	     //compose the message  
	      try{

	         MimeMessage message = new MimeMessage(session);  
	         message.setFrom(new InternetAddress(from));  
	         
	         //add multiple recipients to email
	         InternetAddress[] addressTo = new InternetAddress[toAddress.length];
	         for (int i = 0; i < toAddress.length; i++)
	         {
	             addressTo[i] = new InternetAddress(toAddress[i]);
	         }
	         
	         InternetAddress[] addressCc = new InternetAddress[ccAddress.length];
	         for (int i = 0; i < ccAddress.length; i++)
	         {
	             addressCc[i] = new InternetAddress(ccAddress[i]);
	         }
	         
	         message.addRecipients(Message.RecipientType.TO, addressTo);
	         message.addRecipients(Message.RecipientType.CC, addressCc);
	         
	         
	         
	   	  	SimpleDateFormat sdfDate = new SimpleDateFormat("MMMMM d, yyyy");
	  	  	Date now = new Date();
	  	  	String todayDate = sdfDate.format(now);

	  	  	
	         message.setSubject(subject+ todayDate);  
	         message.setText(title.toString());  
	         
	         
	         //create a multipart message
	         Multipart multipart = new MimeMultipart();
	         


	         // 1. Create the message part i.e. html table
	         BodyPart htmlBodyPart = new MimeBodyPart();

	         // Now set the html table contents as message part
	         htmlBodyPart.setContent(firstPart+"<H3>"+title+"</H3>"+ secondPart + "</table></body> </html>", "text/html");
	         //add this to multipart message body
	         multipart.addBodyPart(htmlBodyPart); 

	         
	         //2. Create the attachment part
	         for (String fileName : attachmentFiles) {
	        	    MimeBodyPart messageBodyPart = new MimeBodyPart();
	        	 // Attach the files
	        	    DataSource source = new FileDataSource(fileName);
	        	    messageBodyPart.setDataHandler(new DataHandler(source));
	        	    messageBodyPart.setFileName(source.getName());
	        	  //add the attachments part to multipart message body
	        	    multipart.addBodyPart(messageBodyPart);
	        	}

	         // Set the complete message parts
	         message.setContent(multipart);
	         
	         // Send message  
	         Transport.send(message);  
	         System.out.println("Message sent successfully.");  
	  
	      }catch (MessagingException mex) {mex.printStackTrace();}
		
		
		
		
	}//end sendmail method



private static String executeCommand(String command) {

	StringBuffer output = new StringBuffer();

	Process p;
	try {
		p = Runtime.getRuntime().exec(command);
		p.waitFor();
		BufferedReader reader =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String line = "";
		while ((line = reader.readLine())!= null) {
			output.append(line + "\n");
		}

	} catch (Exception e) {
		e.printStackTrace();
	}

	return output.toString();

}

private static String htmlProperties(){
	
	String htmlStyle="<html><head>" +
	"<style type='text/css'>" +
	"table {border-collapse: collapse;width:100%;border: 1px solid #000000;}" +
	"td {border: 1px solid #000000;font-family: 'Seagoe UI';background:#ffffff;padding:0; margin:0;}"+
	"th {padding: 0;}"+
	"p{text-align:center;}"+
	"</style></head>";
	
	String htmlBody="<body><table>"+
	"<tr style='border:solid #000000;background:#000000;font-color:#ffffff;font-family: \"Seagoe UI\";'>"+
	"<th><p>Entity</p></th>"+
    "<th><p>Check</p></th>"+
    "<th><p>Description</p></th>"+
    "<th><p>Type/Status</p></th>"+
    "<th><p>Value</p></th>"+"</tr>";
	
	String finalstring=htmlStyle+htmlBody;
	return finalstring;

}
}


