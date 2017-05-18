package fr.licence;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.appengine.api.datastore.Key;

import org.glassfish.jersey.server.model.Resource;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import fr.licence.model.Account;
import fr.licence.model.Book;
import fr.licence.persistence.PMF;

@Path("/shopping")
public class ShoppingService {
	
	@GET
	@Produces("text/plain")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getBook(@QueryParam("account") String account,@QueryParam("isbn") String isbn,@QueryParam("quantite") String quantite) throws JSONException
	{
		String usage = "Usage : account, isbn, quantite";
		
		PersistenceManager pm = PMF.get().getPersistenceManager();

	
		Query accountQuery = pm.newQuery(Account.class);
		accountQuery.setFilter("accountKey == accountParam");
		accountQuery.declareParameters("String accountParam");
		
		
		String accountResult = "";
		String stockResult = "";
		
		
		
		Response response = null;
		try{
			int qte = Integer.parseInt(quantite);
			List<Account> liste = (List<Account>) accountQuery.execute(account);	
			Book entity = null;
			if(!liste.isEmpty()){
				Client client = ClientBuilder.newClient( );
				WebTarget webTarget = client.target("http://1-dot-inf63app9.appspot.com/rest/stock?isbn="+isbn+"&corr="+account);

				// On récupère account : on l'envoi dans corr à stock service.
				// Stock service récupère corr, et le renvoie 
				// On vérifie ici qu'ils sont identiques
				Response r = webTarget.request().get();
				String s = r.readEntity(String.class);
				if(r.getStatus() == 404){
					response = Response.status(r.getStatus()).entity(s).build();
				}
				
				else{
					JSONObject obj = new JSONObject(s);
					String bookIsbn = obj.getString("isbn");
					int bookStock = obj.getInt("stock");
					String corr = obj.getString("corr");
					if(corr.equals(account)){						
						webTarget = client.target("https://whole-saler-app.herokuapp.com/wholeSaler?isbn="+bookIsbn+"&quantite="+quantite+"&corr="+account+"&stock="+bookStock);
						response = webTarget.request().get();
						obj = new JSONObject(response.readEntity(String.class));
						bookIsbn = obj.getString("isbn");
						bookStock = obj.getInt("stock");
						corr = obj.getString("corr");
						String message = obj.getString("message");
						if(corr.equals(account)){
							Book b = new Book(bookIsbn,bookStock);
							String messageRetour = b.toString();
							messageRetour += message;
							response = Response.status(200).entity(messageRetour).build();
						}else{
							response = Response.status(401).entity("invalid corr").build();
						}
					}else{
						response = Response.status(401).entity("invalid corr").build();
					}
				}
				
			}else{
				response = Response.status(401).entity("invalid Account").build();
			}
		}catch(Exception e){
			response = Response.status(400).entity(usage).build();
		}
		finally{
			accountQuery.closeAll();
		}
		return response;
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateStock(String jsonRequest) throws JSONException
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query q = pm.newQuery(Book.class);
		q.setFilter("isbn == isbnParam");
		q.declareParameters("String isbnParam");
		
		JSONObject obj = new JSONObject(jsonRequest);
		String bookIsbn = obj.getString("isbn");
		int bookStock = obj.getInt("stock");
		String corr = obj.getString("corr");
		
		List<Book> liste = (List<Book>) q.execute(bookIsbn);	
		Book entity = null;
		if(!liste.isEmpty()){
			for(Book b : liste){
				entity = b;
			}
		}
	    try {
	        Book b = pm.getObjectById(Book.class, entity.getKey());
	        b.setStock(bookStock);
	    } finally {
	        pm.close();
	    }
	    
		Response response = Response.status(200).entity(jsonRequest).build();
		
		return response;

	}
	
}
