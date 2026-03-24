public class test {
    public static void main(String[] args) throws InterruptedException {
        int[] arr = new int[]{10, 17, 62, 19, 15, 26, 17, 5, 51};
        int sum = 9;
        for (int x : arr) {
            sum += x;
        }
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                sum += arr[i] * arr[j];
            }
        }
        System.out.println(sum);
    }
}