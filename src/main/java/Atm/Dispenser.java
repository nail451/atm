package Atm;

import java.util.HashMap;

public class Dispenser extends Atm {
    private boolean billsInTheDispenser;
    private short billsInTheDispenserCount;
    private HashMap<Integer, Integer> arrayOfBillsInTheDispenser = new HashMap<>();
}
