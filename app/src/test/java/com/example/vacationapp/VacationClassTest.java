package com.example.vacationapp;

import static org.junit.Assert.assertEquals;

import com.example.vacationapp.entities.Vacation;

import org.junit.Test;

public class VacationClassTest {

        @Test
        public void testVacationConstructor() {
            Vacation v = new Vacation(
                    "Hawaii Trip",
                    "Hilton",
                    "2026-05-01",
                    "2026-05-10"
            );

            assertEquals("Hawaii Trip", v.getTitle());
            assertEquals("Hilton", v.getHotel());
            assertEquals("2026-05-01", v.getStartDate());
            assertEquals("2026-05-10", v.getEndDate());
        }
    }