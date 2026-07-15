package com.example.WorkTopus.projects.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DashboardDayResponse(
        LocalDate date,
        String dayOfWeekLabel,
        boolean saturday,
        boolean sunday,
        List<DashboardScheduleResponse> schedules
) {

    public String dayClass() {
        if (saturday) {
            return "calendar__day calendar__day--sat";
        }

        if (sunday) {
            return "calendar__day calendar__day--sun";
        }

        return "calendar__day";
    }
}
