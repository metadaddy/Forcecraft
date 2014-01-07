package us.forcecraft;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import static argo.jdom.JsonNodeFactories.*;

/*
 * Methods for interacting with the Force.com REST API
 */

public class ForceRestClient {
	static JdomParser parser = new JdomParser();
	private static PrettyJsonFormatter formatter = new PrettyJsonFormatter();
	JsonNode oauth;

    void login(String username, String password) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
			//player.addChatMessage("Logging in to Salesforce as "+username);
			
            HttpPost httpPost = new HttpPost("https://"+System.getenv("SF_LOGINHOST")+"/services/oauth2/token");

            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("grant_type", "password"));
            nvps.add(new BasicNameValuePair("username", username));
            nvps.add(new BasicNameValuePair("password", password));
            // TODO - get from config
            nvps.add(new BasicNameValuePair("client_id", "3MVG9Km_cBLhsuPzTtcGHsZpj9JylyezngYKNi.dNkSQmA0fAdwMD9OzkQEPFDJv1UgVF5tcERKtuiP5Yiin3"));
            nvps.add(new BasicNameValuePair("client_secret", "6135262856068035680"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                }

            };

            System.out.println("executing POST " + httpPost.getURI());
            
            String responseBody = httpclient.execute(httpPost, responseHandler);
            System.out.println("----------------------------------------");
            oauth = parser.parse(responseBody);
            Map<JsonStringNode,JsonNode> fieldMap = oauth.getFields();
            for (Map.Entry<JsonStringNode,JsonNode> entry : fieldMap.entrySet())
            {
                System.out.println(entry.getKey().getText() + ": " + entry.getValue().getText());
            }
            System.out.println("----------------------------------------");

        } finally {
            httpclient.close();
        }
    }
    
	JsonNode getAccounts() throws Exception {
		JsonRootNode root = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	String query = "SELECT Name, Id, "+
    				"(SELECT Id, Name, Amount, StageName, IsClosed FROM Account.Opportunities), "+
        			"(SELECT Id, Name FROM Account.Contacts) "+
    				"FROM Account";
            HttpGet httpget = new HttpGet(oauth.getStringValue("instance_url")+
            		"/services/data/v29.0/query?q="+URLEncoder.encode(query, "UTF-8"));
            
            httpget.addHeader("Authorization", "Bearer "+oauth.getStringValue("access_token"));
            

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            System.out.println("executing GET " + httpget.getURI());

            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            root = parser.parse(responseBody);
            System.out.println(formatter.format(root));
            System.out.println("----------------------------------------");
        	
        } finally {
            httpclient.close();
        }
        
        return root;
	}
	
	List<JsonNode> getStages() throws Exception {
		JsonRootNode root = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(oauth.getStringValue("instance_url")+
            		"/services/data/v29.0/sobjects/Opportunity/describe");
            
            httpget.addHeader("Authorization", "Bearer "+oauth.getStringValue("access_token"));

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            System.out.println("executing GET " + httpget.getURI());

            String responseBody = httpclient.execute(httpget, responseHandler);
            root = parser.parse(responseBody);
            
            for (JsonNode field : root.getNode("fields").getElements()) {
            	if (field.getStringValue("name").equals("StageName")) {
            		JsonNode picklistValues = field.getNode("picklistValues");
                    System.out.println("----------------------------------------");
                    System.out.println(formatter.format(picklistValues.getRootNode()));
                    System.out.println("----------------------------------------");
            		return picklistValues.getElements();
            	}
            }
        	
        } finally {
            httpclient.close();
        }
        
        return null;
	}
	
	void getId() throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(oauth.getStringValue("id"));
            
            httpget.addHeader("Authorization", "Bearer "+oauth.getStringValue("access_token"));

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            System.out.println("executing GET " + httpget.getURI());

            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            JsonRootNode root = parser.parse(responseBody);
            Map<JsonStringNode,JsonNode> fieldMap = root.getFields();
            for (Map.Entry<JsonStringNode,JsonNode> entry : fieldMap.entrySet())
            {
                //System.out.println(entry.getKey().getText() + ": " + entry.getValue().getText());
            }
            System.out.println("----------------------------------------");
            String displayName = root.getStringValue("display_name"); 
    		//player.addChatMessage("Hello " + displayName);

        } finally {
            httpclient.close();
        }		
	}
	
	public void setOpportunityStage(String id, String stage) {
		JsonRootNode root = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	HttpPost httppost = new HttpPost(oauth.getStringValue("instance_url")+
            		"/services/data/v29.0/sobjects/Opportunity/"+id+"?_HttpMethod=PATCH");
            
            httppost.addHeader("Authorization", "Bearer "+oauth.getStringValue("access_token"));
            httppost.setEntity(new StringEntity(formatter.format(object(field("StageName", string(stage)))),
                        ContentType.create("application/json", Consts.UTF_8)));

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        return response.getStatusLine().toString();
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            System.out.println("executing POST " + httppost.getURI());

            String responseBody = httpclient.execute(httppost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
        } finally {
        	try {
        		httpclient.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
	}

	public boolean streamingTopicExists() throws Exception {
		JsonRootNode root = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
			System.out.println("Checking for PushTopic "+StreamingClient.TOPIC_NAME);
			
        	String query = "SELECT Id FROM PushTopic WHERE Name = '"+StreamingClient.TOPIC_NAME+"'";
            HttpGet httpget = new HttpGet(oauth.getStringValue("instance_url")+
            		"/services/data/v29.0/query?q="+URLEncoder.encode(query, "UTF-8"));
            
            httpget.addHeader("Authorization", "Bearer "+oauth.getStringValue("access_token"));
            

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            System.out.println("executing GET " + httpget.getURI());

            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            root = parser.parse(responseBody);
            System.out.println(formatter.format(root));
            System.out.println("----------------------------------------");
        	
        } finally {
            httpclient.close();
        }
        
        return (root != null && root.getNode("records").getElements().size() > 0);	
	}

	public void createStreamingTopic() throws Exception {
		JsonRootNode root = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
			System.out.println("Creating PushTopic "+StreamingClient.TOPIC_NAME);
			
        	HttpPost httppost = new HttpPost(oauth.getStringValue("instance_url")+
            		"/services/data/v29.0/sobjects/PushTopic");
            
            httppost.addHeader("Authorization", "Bearer "+oauth.getStringValue("access_token"));
            JsonRootNode json = object(
                field("Name", string(StreamingClient.TOPIC_NAME)),
                field("Query", string(StreamingClient.TOPIC_QUERY)),
                field("ApiVersion", string(StreamingClient.API_VERSION))
            );
            httppost.setEntity(new StringEntity(formatter.format(json),
                    ContentType.create("application/json", Consts.UTF_8)));

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        return response.getStatusLine().toString();
                    } else {
                    	HttpEntity entity = response.getEntity();
                    	System.err.println("HTTP error "+status+"\n"+(entity != null ? EntityUtils.toString(entity) : null));
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };

            System.out.println("executing POST " + httppost.getURI());

            String responseBody = httpclient.execute(httppost, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
        } finally {
        	try {
        		httpclient.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
	}

}
