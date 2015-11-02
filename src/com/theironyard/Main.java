package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static void insertBeer (Connection conn , String name, String type) throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES(? , ?) ");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }

    static void deleteBeer (Connection conn , int selectNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE ROWNUM = ?");
        stmt.setInt(1, selectNum);
        stmt.execute();
    }
    static ArrayList<Beer> selectBeers(Connection conn) throws SQLException{
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * from beers");
        ArrayList<Beer> beers = new ArrayList();
        int id = 1;
        while (results.next()){
            String name = results.getString("name");
            String type = results.getString("type");
            Beer beer = new Beer();
            beer.id = id;
            beer.name = name;
            beer.type = type;
            beers.add(beer);
            id++;
        }
        return beers;
    }

    static void updateBeer (Connection conn, int selectNum, String name, String type)throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ? AND type = ? WHERE ROWNUM = ?");
        stmt.setString(1 , name);
        stmt.setString(2, type);
        stmt.setInt(3, selectNum);
        stmt.execute();
    }


    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt. execute("CREATE TABLE IF NOT EXISTS beers(name VARCHAR , type VARCHAR)");

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    ArrayList<Beer> beers = selectBeers(conn);
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", beers);
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();
                    //beer.id = beers.size() + 1;
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    //beers.add(beer);
                    insertBeer(conn, beer.name, beer.type );
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeer(conn, idNum);
                        //beers.remove(idNum-1);
                        //for (int i = 0; i < beers.size(); i++) {
                        //beers.get(i).id = i + 1;

                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String edit = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(edit);
                        String name = request.queryParams("beername");
                        String type = request.queryParams("beertype");

                    } catch (Exception e){

                    }
                    response.redirect("/");
                    return "";

                })
        );
    }
}
