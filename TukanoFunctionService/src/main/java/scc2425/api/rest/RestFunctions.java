package scc2425.api.rest;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(RestFunctions.PATH)
public interface RestFunctions {

    String PATH = "/function";
    String BLOB_ID = "blobId";
    String TOKEN = "token";

    @GET
    @Path("/{" + BLOB_ID + "}")
    @Produces(MediaType.APPLICATION_JSON)
    Response incrementDownloadCounter(@PathParam(BLOB_ID) String shortId);
}
