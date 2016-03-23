package com.example.santiago.http_requests.http;

import android.os.AsyncTask;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * 
 * Clase para hacer pedidos de okHttp y parsear el resultado en un AsyncTask. Deben sobreescribirse
 *  los metodos getRequest(), parseResponse() y onRequestCompleted(). getRequest() debe devolver 
 *  una referencia no nula de un objecto Request, de lo contrario onRequestCompleted()
 *  se llamará con un NullPointerException. parseResponse() debe devolver el objeto parseado o null 
 *  si hubo un error de parseo. Tanto getRequest() como parseResponse() se ejecutan en el AsyncTask.
 *
 */
public abstract class OkHttpAsyncParsedRequest<E> {
	
	private OkHttpRequestTask task = new OkHttpRequestTask();
	
	/**
	 * Ejecuta en un AsyncTask el pedido okHttp
	 */
	public void execute(){
		task.execute((Void) null);
	}

    /**
     * Metodo a sobreescribir, Se debe devolver una referencia
     * no nula de un objeto OkHttpClient, de lo contrario no habra cliente a ejecutar
     * la request. Este metodo se llama dentro del AsyncTask.
     *
     * @return OkHttpClient a ejecutar la request
     */
    protected OkHttpClient getHttpClient(){
        return new OkHttpClient();
    };

	/**
	 * Metodo a sobreescribir. Se debe devolver una referencia
	 *  no nula de un objecto Request, de lo contrario onRequestCompleted()
	 *  se llamará con un NullPointerException. Este metodo se llama dentro del AsyncTask.
	 *  
	 * @return Request a ejecutarse en el AsyncTask
	 */
	abstract protected Request getRequest();

	/**
	 * Metodo a sobreescribir si se va a usar una authentication. Se debe devolver un authenticator (no nulo)
	 * con el cual se autenticara (mediante oauth2 o basic o cualsea) el pedido.
	 *
	 * Ejemplo de uso con basic (en una clase hija overrideand):
	 *
	 @Override
	 protected Authenticator getAuthenticator() {
		 return new Authenticator() {
			 @Override
			 public Request authenticate(Proxy proxy, Response response) throws IOException {
				 String credential = Credentials.basic(username, password);
				 return response.request().newBuilder()
				 	.header("Authorization", credential)
				 	.build();
			 }

			 @Override
			 public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
			 	return null;
			 }
		 };
	 }
	 * If we want an oauth2, just use isntead of credential string your own "Bearer TOKEN" and its the same
	  *
	 * @note: Si se devuelve nulo no se usara ningun autenticador.
	 * @return Authenticator con los parametros a loggear
	 */
	abstract protected Authenticator getAuthenticator();

	/**
	 * Metodo a sobreescribir. Se debe devolver el objeto parseado no nulo. En caso de haber un error de parseo se
	 *  debe lanzar un HttpParseException, pudiendose agregar como causa otra excepcion
	 *
	 * @note El body lo sacas haciendo response.body().string(). Ojo, es un fd asi que solo lo podes hacer una vez
	 * ya que despues se flushea y no esta mas. Asegurate de guardarlo sobre una variable y no en un Log o algo asi
	 * 
	 * @param response objeto resultante del pedido de donde obtener los datos a parsear
	 * @return objeto ya parseado en la clase especificada, nulo si hubo un error de parseo
	 * @throws org.json.JSONException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	abstract protected E parseResponse(HttpResponse response) throws HttpParseException;
	
	/**
	 * Metodo a sobreescribir. Se devuelve el Response resultante del pedido y la 
	 * exception que se haya lanzado, o null si no se lanzó ninguna
	 * 
	 * @param httpResponse objeto resultante del pedido okHttp, nulo en caso de una excepcion
	 * @param parsedResponse resultado del pedido ya parseado, nulo si hubo un error de parseo
	 * @param exception excepcion que haya detenido el pedido, nulo en caso de no haberse detenido
	 */
	abstract protected void onRequestCompleted(HttpResponse httpResponse, E parsedResponse, Exception exception);
	
	private class OkHttpRequestTask  extends AsyncTask<Void, Void, OkHttpRequestTaskResponse > {
		
		@Override
		protected OkHttpRequestTaskResponse  doInBackground(Void... params) {
			
			OkHttpRequestTaskResponse result = new OkHttpRequestTaskResponse();
			
			try {

                OkHttpClient httpClient = getHttpClient();
				Request request = getRequest();
				Authenticator authenticator = getAuthenticator();
				
				if(httpClient!=null && request!=null) {

					if(authenticator!=null)
						httpClient.setAuthenticator(authenticator);

					Response response = httpClient.newCall(request).execute();
					
					result.httpResponse = new HttpResponse(response);
					result.exception = null;
					
					result.parsedReponse = parseResponse(result.httpResponse);
					
				} else {
					
					result.httpResponse = null;
					result.exception = new NullPointerException("getHttpClient() and getRequest() must return a non null object");
					
					result.parsedReponse = null;
					
				}
				
			
			} catch (IOException | HttpParseException exception) {
				exception.printStackTrace();
				
				result.exception = exception;
				
				result.parsedReponse = null;
				
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(OkHttpRequestTaskResponse result) {
			
			onRequestCompleted(result.httpResponse, result.parsedReponse, result.exception);
			
		}
		
	}
	
	private class OkHttpRequestTaskResponse {
		
		public HttpResponse httpResponse = null;
		public Exception exception = null;
		public E parsedReponse = null;
		
	}
	
}

	
