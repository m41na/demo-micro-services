package works.hop.rest;

import works.hop.search.SearchApp;

public class RestApp {

    private final SearchApp service;

    public RestApp(SearchApp service) {
        this.service = service;
    }

    public static void main(String[] args) {
        RestApp app = new RestApp(SearchApp.instance());
        System.out.println(app.service.search("one"));
    }
}
