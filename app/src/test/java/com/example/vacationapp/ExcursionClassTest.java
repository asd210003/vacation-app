package com.example.vacationapp;

import static org.junit.Assert.assertEquals;

import com.example.vacationapp.entities.Excursion;

import org.junit.Test;

public class ExcursionClassTest {

    @Test
    public void testExcursionConstructor() {
        Excursion e = new Excursion(
                "2026-01-01",
                "Restaurant",
                5
        );

        assertEquals("2026-01-01", e.getDate());
        assertEquals("Restaurant", e.getTitle());
        assertEquals(5, e.getVacationID());
    }
}
