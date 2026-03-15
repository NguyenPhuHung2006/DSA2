public class Outcast {
    private WordNet wordnet;
    public Outcast(WordNet wordnet) {
        if (wordnet == null)
            throw new IllegalArgumentException();
        this.wordnet = wordnet;
    }
    public String outcast(String[] nouns) {
        if (nouns == null)
            throw new IllegalArgumentException();

        int maxDist = -1;
        String outcast = null;

        for (String a : nouns) {
            int sum = 0;

            for (String b : nouns) {
                sum += wordnet.distance(a, b);
            }

            if (sum > maxDist) {
                maxDist = sum;
                outcast = a;
            }
        }

        return outcast;
    }
}