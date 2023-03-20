package solid;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * A CArtAgO artifact that agent can use to interact with LDP containers in a Solid pod.
 */
public class Pod extends Artifact {

    private String podURL; // the location of the Solid pod 

  /**
   * Method called by CArtAgO to initialize the artifact. 
   *
   * @param podURL The location of a Solid pod
   */
    public void init(String podURL) {
        this.podURL = podURL;
        log("Pod artifact initialized for: " + this.podURL);
    }

  /**
   * CArtAgO operation for creating a Linked Data Platform container in the Solid pod
   *
   * @param containerName The name of the container to be created
   * 
   */
    @OPERATION
    public void createContainer(String containerName) {
        // log("method createContainer()");
        if(ressourceExists(URI.create("https://solid.interactions.ics.unisg.ch/valentinhsg/" + containerName + "/"))){
            log("Container exists already");
            return;
        } 

        String requestBody = "@prefix ldp: <http://www.w3.org/ns/ldp#>.\n"+
            "@prefix dcterms: <http://purl.org/dc/terms/>.\n" +
            "<> a ldp:Container, ldp:BasicContainer, ldp:Resource;\n" +
            "dcterms:title \"" + containerName + "\";\n" +
            "dcterms:description \"Container for Exercise 4 created by a jacomo agent\".";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://solid.interactions.ics.unisg.ch/valentinhsg/"))
            .header("Content-Type", "text/turtle")
            .header("Link", "<http://www.w3.org/ns/ldp#BasicContainer>; rel=\"type\"")
            .header("Slug", containerName + "/")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 201) {
                log("Container created sucessfully.");
            } else {
                log("A problem occured while creating the container, response status: " + response.body());
            }
        } catch (IOException | InterruptedException  e) {
            e.printStackTrace();
        }
    }

  /**
   * CArtAgO operation for publishing data within a .txt file in a Linked Data Platform container of the Solid pod
   * 
   * @param containerName The name of the container where the .txt file resource will be created
   * @param fileName The name of the .txt file resource to be created in the container
   * @param data An array of Object data that will be stored in the .txt file
   */
    @OPERATION
    public void publishData(String containerName, String fileName, Object[] data) {
        // log("method publishData()");
        // log(containerName);
        // log(fileName);

        URI fileUri = URI.create("https://solid.interactions.ics.unisg.ch/valentinhsg/" + containerName + "/" + fileName);

        String requestBody = "";
        for (Object obj : data) requestBody += obj.toString() + "\n";

        try {
            HttpClient client = HttpClient.newHttpClient();
            if(ressourceExists(fileUri)) {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(fileUri)
                    .header("Content-Type", "text/plain")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                    client.send(request, HttpResponse.BodyHandlers.ofString());
            } else {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://solid.interactions.ics.unisg.ch/valentinhsg/" + containerName + "/"))
                    .header("Content-Type", "text/plain")
                    .header("Slug", fileName)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                    client.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Boolean ressourceExists(URI fileUri) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(fileUri)
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200){
                // log("File Exists: " + fileUri.toString());
                return true;
            } 
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        // log("File doesn't Exists: " + fileUri.toString());
        return false;
    }

  /**
   * CArtAgO operation for reading data of a .txt file in a Linked Data Platform container of the Solid pod
   * 
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be read
   * @param data An array whose elements are the data read from the .txt file
   */
    @OPERATION
    public void readData(String containerName, String fileName, OpFeedbackParam<Object[]> data) {
        data.set(readData(containerName, fileName));
    }

  /**
   * Method for reading data of a .txt file in a Linked Data Platform container of the Solid pod
   * 
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be read
   * @return An array whose elements are the data read from the .txt file
   */
    public Object[] readData(String containerName, String fileName) {
        // log("3. Implement the method readData(). Currently, the method returns mock data");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://solid.interactions.ics.unisg.ch/valentinhsg/" + containerName + "/" + fileName))
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            // log(response.body());
            return response.body().split("\n");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return new Object [0];
    }

  /**
   * Method that converts an array of Object instances to a string, 
   * e.g. the array ["one", 2, true] is converted to the string "one\n2\ntrue\n"
   *
   * @param array The array to be converted to a string
   * @return A string consisting of the string values of the array elements separated by "\n"
   */
    public static String createStringFromArray(Object[] array) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : array) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }

  /**
   * Method that converts a string to an array of Object instances computed by splitting the given string with delimiter "\n"
   * e.g. the string "one\n2\ntrue\n" is converted to the array ["one", "2", "true"]
   *
   * @param str The string to be converted to an array
   * @return An array consisting of string values that occur by splitting the string around "\n"
   */
    public static Object[] createArrayFromString(String str) {
        return str.split("\n");
    }


  /**
   * CArtAgO operation for updating data of a .txt file in a Linked Data Platform container of the Solid pod
   * The method reads the data currently stored in the .txt file and publishes in the file the old data along with new data 
   * 
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be updated
   * @param data An array whose elements are the new data to be added in the .txt file
   */
    @OPERATION
    public void updateData(String containerName, String fileName, Object[] data) {
        Object[] oldData = readData(containerName, fileName);
        Object[] allData = new Object[oldData.length + data.length];
        System.arraycopy(oldData, 0, allData, 0, oldData.length);
        System.arraycopy(data, 0, allData, oldData.length, data.length);
        publishData(containerName, fileName, allData);
    }
}
