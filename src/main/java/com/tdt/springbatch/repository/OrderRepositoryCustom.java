package com.tdt.springbatch.repository;

public interface OrderRepositoryCustom {
    public int[] batchInsert(int recordsStart, int recordsStop);
}
