package tukano.impl;

import java.util.logging.Logger;

import tukano.api.Blobs;
import tukano.api.Result;
import tukano.impl.rest.TukanoRestServer;
import tukano.impl.storage.BlobStorage;
import tukano.impl.storage.FilesystemStorage;
import utils.Hash;
import utils.Hex;

import static java.lang.String.format;
import static tukano.api.Result.ErrorCode.FORBIDDEN;
import static tukano.api.Result.error;

public class JavaBlobs implements Blobs {

    private static Blobs instance;
    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());
    private static final String BLOB_SERVICE_HOST = "blob-service";
    private static final String BLOB_SERVICE_PORT = "8080";
    private static final String BLOB_SERVICE_BASE_URL = "http://" + BLOB_SERVICE_HOST + ":" + BLOB_SERVICE_PORT;

    private BlobServiceClient blobServiceClient;
    public String baseURI;

    synchronized public static Blobs getInstance() {
        if (instance == null)
            instance = new JavaBlobs();
        return instance;
    }

    private JavaBlobs() {
        String BLOB_ENDPOINT = BLOB_SERVICE_BASE_URL + "/blob-service-1/rest/blobs";
        blobServiceClient=new BlobServiceClient(BLOB_ENDPOINT);
        baseURI = String.format("%s/%s/", TukanoRestServer.serverURI, Blobs.NAME);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes, String token) {
        Log.info(() -> format("upload : blobId = %s, sha256 = %s, token = %s\n", blobId, Hex.of(Hash.sha256(bytes)), token));

        try {
            var response = blobServiceClient.upload(blobId, bytes, token);
            System.out.println(response.statusCode());
            System.out.println("upload javaBlobs");
            boolean isSuccess = response.statusCode() >= 200 && response.statusCode() < 300;
            return isSuccess ? Result.ok() : Result.error(Result.ErrorCode.INTERNAL_ERROR);
        } catch (Exception e) {
            Log.severe(() -> "Error uploading blob: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String blobId, String token) {
        Log.info(() -> format("download : blobId = %s, token=%s", blobId, token));
        try {
            var response = blobServiceClient.download(blobId, token);
            boolean isSuccess = response.statusCode() >= 200 && response.statusCode() < 300;
            return isSuccess ? Result.ok(response.body().getBytes()) : Result.error(Result.ErrorCode.INTERNAL_ERROR);
        } catch (Exception e) {
            Log.severe(() -> "Error downloading blob: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> delete(String blobId, String token) {
        Log.info(() -> format("delete : blobId = %s, token=%s\n", blobId, token));

        try {
            var response = blobServiceClient.delete(blobId, token);
            boolean isSuccess = response.statusCode() >= 200 && response.statusCode() < 300;
            return isSuccess ? Result.ok() : Result.error(Result.ErrorCode.INTERNAL_ERROR);
        } catch (Exception e) {
            Log.severe(() -> "Error deleting blob: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteAllBlobs(String userId, String token) {
        Log.info(() -> format("deleteAllBlobs : userId = %s, token=%s\n", userId, token));
        try {
            var response = blobServiceClient.deleteAllBlobs(userId, token);
            boolean isSuccess = response.statusCode() >= 200 && response.statusCode() < 300;
            return isSuccess ? Result.ok() : Result.error(Result.ErrorCode.INTERNAL_ERROR);
        } catch (Exception e) {
            Log.severe(() -> "Error deleting blob: " + e.getMessage());
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
}
