package works.hop.repo;

public class RepoApp {

    private static RepoApp repo;

    public static void main(String[] args) {
        System.out.println("Hello from RepoApp!");
    }

    public static RepoApp instance() {
        if (repo == null) {
            repo = new RepoApp();
        }
        return repo;
    }

    public String fetch(String criteria) {
        return String.format("searching %s and returning %s", criteria, "from RepoApp");
    }
}
