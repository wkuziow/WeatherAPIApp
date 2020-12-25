package pl.kuziow.weatherapiapp;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String QUERY_FOR_CITY_ID = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_CITY_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";


    Context context;

    String cityID;

    public WeatherDataService(Context context) {
        this.context = context;
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String cityID);
    }

    public void getCityID(String cityName, VolleyResponseListener volleyResponseListener) {

        String url = QUERY_FOR_CITY_ID + cityName;


        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cityID = "";

                try {
                    JSONObject cityInfo = response.getJSONObject(0);
                    cityID = cityInfo.getString("woeid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //  Toast.makeText(context, "City Id: " + cityID, Toast.LENGTH_SHORT).show();
                volleyResponseListener.onResponse(cityID);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //    Toast.makeText(context, "something wrong.", Toast.LENGTH_SHORT).show();
                volleyResponseListener.onError("Something wrong");
            }
        });

        MySingleton.getInstance(context).addToRequestQueue(request);

        //  return cityID;
    }

    public interface ForecastByIDResponse {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModel);
    }

    public void getCityForecastByID(String cityID, ForecastByIDResponse forecastByIDResponse) {

        List<WeatherReportModel> weatherReportModels = new ArrayList<>();

        String url = QUERY_FOR_CITY_WEATHER_BY_ID + cityID;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {
                // Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();

                try {
                    JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");


                    for (int i = 0; i < consolidated_weather_list.length(); i++) {
                        WeatherReportModel oneDay = new WeatherReportModel();


                        JSONObject firstDayFromApi = (JSONObject) consolidated_weather_list.get(i);
                        oneDay.setId(firstDayFromApi.getInt("id"));
                        oneDay.setApplicable_date(firstDayFromApi.getString("applicable_date"));
                        oneDay.setWeather_state_name(firstDayFromApi.getString("weather_state_name"));

                        oneDay.setMin_temp(firstDayFromApi.getLong("min_temp"));
                        oneDay.setMax_temp(firstDayFromApi.getLong("max_temp"));
                        oneDay.setThe_temp(firstDayFromApi.getLong("the_temp"));


                        weatherReportModels.add(oneDay);
                    }
                    forecastByIDResponse.onResponse(weatherReportModels);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MySingleton.getInstance(context).addToRequestQueue(request);

    }

    public interface GetCityForecastByCityNameCallback {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModes);
    }

    public void getCityForecastByName(String cityName, GetCityForecastByCityNameCallback getCityForecastByCityNameCallback) {
        getCityID(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(String cityID) {
                getCityForecastByID(cityID, new ForecastByIDResponse() {
                    @Override
                    public void onError(String message) {

                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModels) {
                        getCityForecastByCityNameCallback.onResponse(weatherReportModels);
                    }
                });

            }
        });

    }
}