public class NullPointerExceptionExample {
    public static void main(String[] args) {        
        try {
            String str = String.valueOf(null);
            System.out.println("String value: " + str);
        } catch (NullPointerException e) {
            System.err.println("Caught a NullPointerException!");
            e.printStackTrace();
        }
    }
}
