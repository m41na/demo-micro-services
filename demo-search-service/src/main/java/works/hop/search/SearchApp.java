package works.hop.search;

import works.hop.repo.RepoApp;

public class SearchApp {

    private static SearchApp service;

    private final RepoApp repo;

    public SearchApp(RepoApp repo) {
        this.repo = repo;
    }

    public static void main(String[] args) {
        System.out.println("Hello from SearchApp!");
    }

    public static SearchApp instance() {
        if (service == null) {
            service = new SearchApp(RepoApp.instance());
        }
        return service;
    }

    public String search(String one) {
        return repo.fetch(one);
    }
}
