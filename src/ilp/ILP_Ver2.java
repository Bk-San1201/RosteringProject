package ilp;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import utils.Data;

import java.util.ArrayList;

public class ILP_Ver2 {
    MPSolver mpSolver;
    MPVariable[][][] x;
    public MPVariable s;
    public Data data;

    static {
        System.loadLibrary("jniortools"); // Load the native library.
    }

    public ILP_Ver2(String filename) {
        this.data = new Data(filename);
    }

    private void init() {
        this.mpSolver = new MPSolver("TSP solver", MPSolver.OptimizationProblemType.valueOf("CBC_MIXED_INTEGER_PROGRAMMING"));
        this.x = new MPVariable[data.D][4][data.N];
        this.s = this.mpSolver.makeIntVar(0, Float.POSITIVE_INFINITY, "s");

        for (int i = 0; i < data.D; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < data.N; k++) {
                    this.x[i][j][k] = this.mpSolver.makeIntVar(0, 1, "x[" + i + "][" + j + "][" + k + "]");
                }
            }
        }
    }

    private void makeConstraint() {
//        Moi ngay, moi nhan vien lam nhieu nhat 1 ca
        for (int i = 0; i < data.D; i++) {
            for (int k = 0; k < data.N; k++) {
                if (data.dayOff[k].contains(i)) {
//                    Nhan vien co the nghi lam theo nhu rang buoc
                    MPConstraint mpConstraint = this.mpSolver.makeConstraint(0, 0, "c0[" + i + "][" + k + "]");
                    for (int j = 0; j < 4; j++) {
                        mpConstraint.setCoefficient(this.x[i][j][k], 1);
                    }
                } else {
                    MPConstraint mpConstraint = this.mpSolver.makeConstraint(0, 1, "c0[" + i + "][" + k + "]");
                    for (int j = 0; j < 4; j++) {
                        mpConstraint.setCoefficient(this.x[i][j][k], 1);
                    }
                }
            }
        }
//        Moi ca can co alpha nhan vien den beta nhan vien
        for (int i = 0; i < data.D; i++) {
            for (int j = 0; j < 4; j++) {
                MPConstraint mpConstraint = this.mpSolver.makeConstraint(data.alp, data.bet, "c1[" + i + "][" + j + "]");
                for (int k = 0; k < data.N; k++) {
                    mpConstraint.setCoefficient(this.x[i][j][k], 1);
                }
            }
        }
//        Ngay hom nay lam ca dem thi ngay hom sau duoc nghi
        for (int i = 0; i < data.D - 1; i++) {
            for (int k = 0; k < data.N; k++) {
                MPConstraint mpConstraint = this.mpSolver.makeConstraint(0, 1, "c2[" + i + "][" + k + "]");
//                ca dem
                mpConstraint.setCoefficient(this.x[i][3][k], 1);
//                ngay hom sau
                for (int j = 0; j < 4; j++) {
                    mpConstraint.setCoefficient(this.x[i + 1][j][k], 1);
                }
            }
        }
    }

    public MPObjective makeObjective() {
//        make maximize objective
        for (int k = 0; k < data.N; k++) {
            MPConstraint mpConstraint = this.mpSolver.makeConstraint(0, Float.POSITIVE_INFINITY, "c3[" + k + "]");
            mpConstraint.setCoefficient(this.s, 1);
            for (int i = 0; i < data.D; i++) {
                mpConstraint.setCoefficient(this.x[i][3][k], -1);
            }
        }
//        make objective
        MPObjective mpObjective = this.mpSolver.objective();
        mpObjective.setCoefficient(this.s, 1);
        mpObjective.setMinimization();
        return mpObjective;
    }

    public void run() {
        this.init();
        this.makeConstraint();
        MPObjective mpObjective = this.makeObjective();

        final MPSolver.ResultStatus resultStatus = this.mpSolver.solve();
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Solution:");
            System.out.println("Objective value = " + mpObjective.value());
            for (int i = 0; i < data.D; i++) {
                for (int j = 0; j < 4; j++) {
                    System.out.print("Day[" + (i) + "] --- Part[" + (j) + "] = {");
                    for (int k = 0; k < data.N; k++) {
                        if (this.x[i][j][k].solutionValue() == 1)
                            System.out.print((k) + " ");
                    }
                    System.out.print("}\n");
                }
                System.out.println("--------------------------------------------");
            }
            System.out.println("Problem solved in " + mpSolver.wallTime() + " milliseconds");
        } else {
            System.out.println("Infeasible");
        }
    }

    public static void main(String[] args) {
        ILP_Ver2 ilp_ver2 = new ILP_Ver2("./data/sample3.txt");
        ilp_ver2.run();
    }
}
