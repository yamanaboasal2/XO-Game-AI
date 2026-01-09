import java.io.*;
import java.util.*;

public class MLPTrainer {


    static final int INPUT = 6;
    static final int H1 = 18;
    static final int H2 = 8;
    static final int OUTPUT = 2;
    static final int EPOCHS = 250;
    static final double LR = 0.03;
    static final double LEAKY = 0.01;
    static final double TRAIN_RATIO = 0.7;
    static final double VAL_RATIO = 0.15;


    static double[][] W1 = new double[H1][INPUT];
    static double[] B1 = new double[H1];

    static double[][] W2 = new double[H2][H1];
    static double[] B2 = new double[H2];

    static double[][] W3 = new double[OUTPUT][H2];
    static double[] B3 = new double[OUTPUT];

    static class Sample {
        double[] x;
        int y;
        Sample(double[] x, int y){ this.x = x; this.y = y; }
    }

    public static void main(String[] args) throws Exception {

        Locale.setDefault(Locale.US);


        List<double[]> Xraw = new ArrayList<>();
        List<Integer> Yraw = new ArrayList<>();

        BufferedReader br = new BufferedReader(
                new FileReader("C:\\Users\\hp\\IdeaProjects\\AI1\\src\\tictactoe_dataset.csv")
        );
        br.readLine();

        String line;
        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");

            double[] row = new double[INPUT];
            for (int i = 0; i < INPUT; i++)
                row[i] = Double.parseDouble(p[i]);

            int raw = Integer.parseInt(p[6]);
            int label = (raw == 1) ? 0 : 1;

            Xraw.add(row);
            Yraw.add(label);
        }
        br.close();


        normalize(Xraw);


        List<Sample> data = new ArrayList<>();
        for (int i = 0; i < Xraw.size(); i++)
            data.add(new Sample(Xraw.get(i), Yraw.get(i)));

        Collections.shuffle(data, new Random(42));

        int N = data.size();
        int trainEnd = (int)(N * TRAIN_RATIO);
        int valEnd   = (int)(N * (TRAIN_RATIO + VAL_RATIO));

        List<Sample> train = data.subList(0, trainEnd);
        List<Sample> val   = data.subList(trainEnd, valEnd);
        List<Sample> test  = data.subList(valEnd, N);


        Random r = new Random(42);
        init(W1, r);
        init(W2, r);
        init(W3, r);

        for (int e = 1; e <= EPOCHS; e++) {

            Collections.shuffle(train, new Random(1000 + e));
            double loss = 0;

            for (int n = 0; n < train.size(); n++) {

                Sample s = train.get(n);
                double[] h1z = new double[H1];
                double[] h1 = new double[H1];
                for (int i = 0; i < H1; i++) {
                    double sum = B1[i];
                    for (int j = 0; j < INPUT; j++)
                        sum += W1[i][j] * s.x[j];
                    h1z[i] = sum;
                    h1[i] = lrelu(sum);
                }

                double[] h2z = new double[H2];
                double[] h2 = new double[H2];
                for (int i = 0; i < H2; i++) {
                    double sum = B2[i];
                    for (int j = 0; j < H1; j++)
                        sum += W2[i][j] * h1[j];
                    h2z[i] = sum;
                    h2[i] = lrelu(sum);
                }

                double[] outz = new double[OUTPUT];
                for (int k = 0; k < OUTPUT; k++) {
                    double sum = B3[k];
                    for (int j = 0; j < H2; j++)
                        sum += W3[k][j] * h2[j];
                    outz[k] = sum;
                }

                double[] p = softmax(outz);
                loss += -Math.log(p[s.y] + 1e-9);


                double[] d3 = new double[OUTPUT];
                for (int k = 0; k < OUTPUT; k++)
                    d3[k] = p[k] - (k == s.y ? 1 : 0);

                double[] d2 = new double[H2];
                for (int i = 0; i < H2; i++) {
                    for (int k = 0; k < OUTPUT; k++)
                        d2[i] += d3[k] * W3[k][i];
                    d2[i] *= dlrelu(h2z[i]);
                }

                double[] d1 = new double[H1];
                for (int i = 0; i < H1; i++) {
                    for (int j = 0; j < H2; j++)
                        d1[i] += d2[j] * W2[j][i];
                    d1[i] *= dlrelu(h1z[i]);
                }


                for (int k = 0; k < OUTPUT; k++)
                    for (int j = 0; j < H2; j++)
                        W3[k][j] -= LR * d3[k] * h2[j];


                for (int i = 0; i < H2; i++)
                    for (int j = 0; j < H1; j++)
                        W2[i][j] -= LR * d2[i] * h1[j];


                for (int i = 0; i < H1; i++)
                    for (int j = 0; j < INPUT; j++)
                        W1[i][j] -= LR * d1[i] * s.x[j];
            }

            if (e == 1 || e % 25 == 0)
                System.out.printf("Epoch %d/%d - loss=%.5f%n", e, EPOCHS, loss / train.size());
        }


        System.out.printf("Train accuracy: %.4f%n", acc(train));
        System.out.printf("Val   accuracy: %.4f%n", acc(val));
        System.out.printf("Test  accuracy: %.4f%n", acc(test));


        int[][] cm = new int[2][2];
        for (Sample s : test)
            cm[s.y][argmax(predict(s.x))]++;

        System.out.println("\nConfusion Matrix on TEST (rows=true, cols=pred) [Xwin, Owin]:");
        System.out.println(Arrays.toString(cm[0]));
        System.out.println(Arrays.toString(cm[1]));
        System.out.println("private static final double[][] W1 = {");
        for (int i = 0; i < H1; i++) {
            System.out.print("  { ");
            for (int j = 0; j < INPUT; j++) {
                System.out.printf("%.6f", W1[i][j]);
                if (j < INPUT - 1) System.out.print(", ");
            }
            System.out.println(" },");
        }
        System.out.println("};\n");


        System.out.println("private static final double[][] W2 = {");
        for (int i = 0; i < H2; i++) {
            System.out.print("  { ");
            for (int j = 0; j < H1; j++) {
                System.out.printf("%.6f", W2[i][j]);
                if (j < H1 - 1) System.out.print(", ");
            }
            System.out.println(" },");
        }
        System.out.println("};\n");

        System.out.println("private static final double[][] W3 = {");
        for (int i = 0; i < OUTPUT; i++) {
            System.out.print("  { ");
            for (int j = 0; j < H2; j++) {
                System.out.printf("%.6f", W3[i][j]);
                if (j < H2 - 1) System.out.print(", ");
            }
            System.out.println(" },");
        }
        System.out.println("};");


    }


    static double lrelu(double x){ return x > 0 ? x : LEAKY * x; }
    static double dlrelu(double x){ return x > 0 ? 1 : LEAKY; }

    static double[] softmax(double[] z){
        double max = Arrays.stream(z).max().getAsDouble();
        double sum = 0;
        double[] o = new double[z.length];
        for (int i = 0; i < z.length; i++){
            o[i] = Math.exp(z[i] - max);
            sum += o[i];
        }
        for (int i = 0; i < z.length; i++) o[i] /= sum;
        return o;
    }

    static int argmax(double[] a){ return a[0] > a[1] ? 0 : 1; }

    static double[] predict(double[] x){
        double[] h1 = new double[H1];
        for (int i = 0; i < H1; i++){
            double s = B1[i];
            for (int j = 0; j < INPUT; j++) s += W1[i][j] * x[j];
            h1[i] = lrelu(s);
        }
        double[] h2 = new double[H2];
        for (int i = 0; i < H2; i++){
            double s = B2[i];
            for (int j = 0; j < H1; j++) s += W2[i][j] * h1[j];
            h2[i] = lrelu(s);
        }
        double[] out = new double[OUTPUT];
        for (int k = 0; k < OUTPUT; k++){
            double s = B3[k];
            for (int j = 0; j < H2; j++) s += W3[k][j] * h2[j];
            out[k] = s;
        }
        return softmax(out);
    }

    static double acc(List<Sample> list){
        int c = 0;
        for (Sample s : list)
            if (argmax(predict(s.x)) == s.y) c++;
        return c / (double) list.size();
    }

    static void init(double[][] W, Random r){
        for (int i = 0; i < W.length; i++)
            for (int j = 0; j < W[i].length; j++)
                W[i][j] = r.nextDouble() - 0.5;
    }

    static void normalize(List<double[]> X){
        int d = X.get(0).length;
        double[] min = new double[d];
        double[] max = new double[d];
        Arrays.fill(min, Double.POSITIVE_INFINITY);
        Arrays.fill(max, Double.NEGATIVE_INFINITY);

        for (double[] r : X)
            for (int i = 0; i < d; i++){
                min[i] = Math.min(min[i], r[i]);
                max[i] = Math.max(max[i], r[i]);
            }

        for (double[] r : X)
            for (int i = 0; i < d; i++)
                if (max[i] > min[i])
                    r[i] = (r[i] - min[i]) / (max[i] - min[i]);
    }
}
