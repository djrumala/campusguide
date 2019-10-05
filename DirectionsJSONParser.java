package com.common_id.campusguide;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class DirectionsJSONParser {

    /**
     * Receives a JSONObject and returns a list of lists containing latitude and longitude
     */
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
//                            Log.e("LatLong", Double.toString(((LatLng)list.get(l)).latitude)+","+
//                                    Double.toString(((LatLng)list.get(l)).longitude));
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }

    public Integer getDuration(JSONObject jObject) {

        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        Integer duration = 0;
        Integer time = 0;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        time = (Integer) ((JSONObject) ((JSONObject) jSteps.get(k)).get("duration")).get("value");
                        duration = duration + time;
//                        Log.e("duration", String.valueOf(duration));
//                        Log.e("time", String.valueOf(k+1)+ " " +String.valueOf(time));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return duration;
    }

    public Integer getDistance(JSONObject jObject) {

        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        Integer distance = 0;
        Integer meter = 0;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        meter = (Integer) ((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("value");
                        distance = distance + meter;
//                        Log.e("total d", String.valueOf(distance));
//                        Log.e("distance", String.valueOf(k+1)+ " " +String.valueOf(meter));
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return distance;
    }

    public List<List<HashMap<String, String>>> getAll(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {
            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String html = jSteps.getJSONObject(k).getString("html_instructions");
                        Integer distance = (Integer) ((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("value");
                        Integer duration = (Integer) ((JSONObject) ((JSONObject) jSteps.get(k)).get("duration")).get("value");
                        Double start_lat = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lat");
                        Double start_lon = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lng");
                        Double end_lat = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).get("lat");
                        Double end_lon = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).get("lng");
                        Log.e("Result", String.valueOf(distance));

                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("html_instructions", html);
                        hm.put("distance", String.valueOf(distance));
                        hm.put("duration", String.valueOf(duration));
                        hm.put("start_lat", String.valueOf(start_lat));
                        hm.put("start_lon", String.valueOf(start_lon));
                        hm.put("end_lat", String.valueOf(end_lat));
                        hm.put("end_lon", String.valueOf(end_lon));
                        path.add(hm);
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}