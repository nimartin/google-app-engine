package fr.licence;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import fr.licence.model.Book;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import fr.licence.persistence.PMF;


@Path("/stock")
public class StockService {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStock(@QueryParam("isbn") String isbn,@QueryParam("corr") String corr) throws JSONException
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		
		
		Query q = pm.newQuery(Book.class);
		q.setFilter("isbn == isbnParam");
		q.declareParameters("String isbnParam");
		
		Response response = null;
		try{
			List<Book> liste = (List<Book>) q.execute(isbn);	
			Book entity = null;
			if(!liste.isEmpty()){
				for(Book b : liste){
					entity = b;
				}
				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("isbn", entity.getIsbn());
				jsonResponse.put("stock", entity.getStock());
				jsonResponse.put("corr",corr);
				response = Response.status(200).type(MediaType.APPLICATION_JSON).entity(jsonResponse.toString()).build();
			}else{
				response = Response.status(404).type(MediaType.APPLICATION_JSON).entity("invalid isbn").build();
			}
		}finally{
			q.closeAll();
		}
		
		return response;

	}
}