package scc2425.impl.rest;


import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import scc2425.api.rest.RestFunctions;
import scc2425.api.rest.Result;
import scc2425.utils.SqlDB;
import scc2425.impl.data.Short;

import java.util.logging.Logger;

import static scc2425.api.rest.Result.errorOrValue;

@Singleton
public class RestFunctionsResource extends RestResource implements RestFunctions {
    final private static Logger Log = Logger.getLogger(RestFunctionsResource.class.getName());

    @Override
    public Response incrementDownloadCounter(String shortId) {
        Log.severe(() -> "Test incrementDownloadCounter");

        try {

            Result<Short> result = errorOrValue( SqlDB.getOne(shortId, scc2425.impl.data.Short.class), shrt -> shrt);

            if (result.isOK()) {

                Short shrt = result.value();
                shrt.incrementTotalViews();

                Result<Short> response = SqlDB.updateOne(shrt);

                System.out.println(response);
            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return Response.ok().build();
    }
}
