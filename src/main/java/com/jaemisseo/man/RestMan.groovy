package com.jaemisseo.man

import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource

/**
 * Created by sujkim on 2017-03-10.
 */
class RestMan {

    static final String GET = 'GET'
    static final String POST = 'POST'
    static final String PUT = 'PUT'
    static final String DELETE = 'DELETE'

    String url = "http://192.168.0.55:19070/erwin_mart9g/getLibraryDepth"
    String type = "*/*"
    String method
    Map headerMap = [:]
    Map parameterMap = [:]



    RestMan setMethod(String method){
        this.method = method
        return this
    }

    RestMan addParameter(Map parameterMapToAdd){
        parameterMapToAdd.each{
            addParameter(it.key, it.value)
        }
        return this
    }

    RestMan addParameter(String key, String value){
        parameterMap[key] = value
        return this
    }

    RestMan addHeader(Map headerMapToAdd){
        headerMapToAdd.each{
            addHeader(it.key, it.value)
        }
        return this
    }

    RestMan addHeader(String key, String value){
        headerMap[key] = value
        return this
    }


    
    /**
     * GET
     */
    def get(String url, String jsonParam) {
        return request(url, GET, jsonParam)
    }

    /**
     * POST
     */
    def post(String url, String jsonParam) {
        return request(url, POST, jsonParam)
    }

    /**
     * PUT
     */
    def put(String url, String jsonParam){
        return request(url, PUT, jsonParam)
    }

    /**
     * DELETE
     */
    def delete(String url, String jsonParam){
        return request(url, DELETE, jsonParam)
    }

    /**
     * REQUEST
     */
    String request(String url, String jsonParam){
        request(url, jsonParam, method)
    }

    String request(String url, String jsonParam, String method){
        String responseString
        try {
            ClientResponse response
            Client client = Client.create()
            WebResource webResource = client.resource(url)
            WebResource.Builder builder = webResource.type(type)
            header(builder, headerMap)

            //REQUEST
            if (!method)
                response = builder.post(ClientResponse.class, jsonParam)
            else if (method == GET)
                response = builder.get(ClientResponse.class)
            else if (method == POST)
                response = builder.post(ClientResponse.class, jsonParam)
            else if (method == PUT)
                response = builder.put(ClientResponse.class, jsonParam)
            else if (method == DELETE)
                response = builder.delete(ClientResponse.class, jsonParam)

            //CHECK ERROR
            checkError(response)

            //RESPONSE
            responseString = response.getEntity(String.class)

        }catch (Exception e){
            e.printStackTrace()
        }
        return responseString
    }



    private header(WebResource.Builder builder, Map headerMap){
        headerMap.each{
            builder.header(it.key, it.value)
        }
    }

    private boolean checkError(ClientResponse response){
        if (response.getStatus() != 200){
            throw new RuntimeException("Failed : HTTP error code : ${response.getStatus()}")
        }
        return true
    }

//    private void updateCustomer(Customer customer) {
//        try {
//            URL url = new URL("http://www.example.com/customers");
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoOutput(true);
//            connection.setInstanceFollowRedirects(false);
//            connection.setRequestMethod("PUT");
//            connection.setRequestProperty("Content-Type", "application/xml");
//
//            OutputStream os = connection.getOutputStream();
//            jaxbContext.createMarshaller().marshal(customer, os);
//            os.flush();
//
//            connection.getResponseCode();
//            connection.disconnect();
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    public String HeaderSender(HttpServletRequest req) {
//        println '///////////////////////'
//        println '///// REST Sender /////'
//        println '///////////////////////'
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> resEnt
//        HttpMethod httpMethod
//        String[] reqAtts = req.getParameterValues('reqAtt')
//        String[] reqVals = req.getParameterValues('reqVal')
//        String[] hdAtts = req.getParameterValues('hdAtt')
//        String[] hdVals = req.getParameterValues('hdVal')
//        String url = req.getParameter('url')
//        String method = req.getParameter('method')
//
//        // httpMethod
//        if (method.equals('GET')) httpMethod = HttpMethod.GET
//        else if (method.equals('POST')) httpMethod = HttpMethod.POST
//        else if (method.equals('PUT')) httpMethod = HttpMethod.PUT
//        else if (method.equals('DELETE')) httpMethod = HttpMethod.DELETE
//        else  httpMethod = HttpMethod.GET
//
//        // Change URL
//        Map<String, String> vars = new HashMap<String, String>();
//
//        // request parameter
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
//        if (reqAtts && reqVals) {
//            if (httpMethod == HttpMethod.GET){
//                if (url.indexOf('?') == -1) url += '?'
//                reqAtts.eachWithIndex{ String att, int idx->
//                    url += "&${att}=${reqVals[idx]}"
//                }
//                for (int i = 0; i < reqAtts.size(); i++) {
//                    if (!reqVals[i]) reqVals[i]='null'
//                    params.add(reqAtts[i], reqVals[i]);
//                }
//            }else{
//                for (int i = 0; i < reqAtts.size(); i++) {
//                    if (!reqVals[i]) reqVals[i]='null'
//                    params.add(reqAtts[i], reqVals[i]);
//                }
//            }
//        }
//
//        // header parameter
//        MultiValueMap<String, String> customHeaders = new LinkedMultiValueMap<String, String>();
//        if (hdAtts && hdVals){
//            for (int i=0; i<hdAtts.size(); i++){
//                if (!hdVals[i]) hdVals[i]='null'
//                customHeaders.add(hdAtts[i], hdVals[i])
//            }
//        }
//
//        // LOG
//        println 'params: ' + params.toString()
//        println 'headers: ' + customHeaders.toString()
//        println 'httpMethod: ' + httpMethod.toString()
//        println 'url: ' + url
//        println ''
//
//        //send request and header and get json
//        try{
//            resEnt = restTemplate.exchange(url, httpMethod, new HttpEntity<MultiValueMap<String, String>>(params, customHeaders), String.class, vars);
//        }catch(ConnectException e){
//            e.printStackTrace()
//            return 'error: Connection refused: connect'
//        }catch(Exception e){
//            e.printStackTrace()
//            return 'error: Maybe, Server is not running'
//        }
//
//        // json 받기
//        String json = (String)resEnt.getBody()
//        return json
//    }

}
