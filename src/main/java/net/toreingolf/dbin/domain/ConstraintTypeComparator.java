package net.toreingolf.dbin.domain;

import java.util.Comparator;

public class ConstraintTypeComparator implements Comparator<AllConstraints> {
    private int sortOrder(AllConstraints constraint) {
        return switch (constraint.getConstraintType()) {
            case "P" -> 1;
            case "U" -> 2;
            case "R" -> 3;
            case "C" -> 4;
            default -> throw new IllegalStateException("Unexpected value: " + constraint.getConstraintType());
        };
    }

    @Override
    public int compare(AllConstraints o1, AllConstraints o2) {
        return Integer.compare(sortOrder(o1), sortOrder(o2));
    }
}

