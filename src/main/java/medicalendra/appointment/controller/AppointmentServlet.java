package medicalendra.appointment.controller;

import medicalendra.appointment.model.Appointment;
import medicalendra.appointment.service.AppointmentService;
import medicalendra.patient.service.PatientService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Servlet handling CRUD actions for Appointment entities.
 * URL patterns:
 *   /appointments               - list appointments (GET)
 *   /appointment/schedule       - schedule appointment page (GET)
 *   /appointment/create         - create appointment (POST, also GET fallback)
 *   /appointment/edit           - edit appointment (POST)
 *   /appointment/delete         - delete appointment (POST)
 *   /appointment/load           - load appointment for editing (GET, id param)
 *   /appointment/search         - search appointments (GET, q param)
 */
@WebServlet(name = "AppointmentServlet",
    urlPatterns = {"/appointments", "/appointment/schedule", "/appointment/create", "/appointment/edit", "/appointment/delete", "/appointment/load", "/appointment/search"})
public class AppointmentServlet extends HttpServlet {
    private AppointmentService appointmentService;
    private PatientService patientService;

    @Override
    public void init() throws ServletException {
        patientService = PatientService.withServletContext(getServletContext());
        appointmentService = AppointmentService.withServletContext(getServletContext(), patientService);
    }

    // ---------- GET handling ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/appointments":
                listAppointments(req, resp);
                break;
            case "/appointment/schedule":
            case "/appointment/create":
                showScheduleForm(req, resp);
                break;
            case "/appointment/load":
                loadAppointment(req, resp);
                break;
            case "/appointment/search":
                searchAppointments(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showScheduleForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("patients", patientService.getAllPatients());
        req.getRequestDispatcher("/appointment-create.jsp").forward(req, resp);
    }

    private void listAppointments(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        req.setAttribute("appointments", appointments);
        // also load patients for patient name resolution
        req.setAttribute("patients", patientService.getAllPatients());
        req.getRequestDispatcher("/appointment-list.jsp").forward(req, resp);
    }

    private void loadAppointment(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        Optional<Appointment> opt = Optional.ofNullable(appointmentService.getAppointmentById(id));
        if (opt.isPresent()) {
            req.setAttribute("editAppointment", opt.get());
            req.setAttribute("patients", patientService.getAllPatients());
            req.getRequestDispatcher("/appointment-edit.jsp").forward(req, resp);
        } else {
            String msg = URLEncoder.encode("Appointment not found", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/appointments?error=" + msg);
        }
    }

    private void searchAppointments(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        List<Appointment> appointments = appointmentService.search(q);
        req.setAttribute("appointments", appointments);
        req.setAttribute("searchQuery", q);
        req.setAttribute("patients", patientService.getAllPatients());
        req.getRequestDispatcher("/appointment-list.jsp").forward(req, resp);
    }

    // ---------- POST handling ----------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        switch (path) {
            case "/appointment/create":
                handleCreate(req, resp);
                break;
            case "/appointment/edit":
                handleEdit(req, resp);
                break;
            case "/appointment/delete":
                handleDelete(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String patientId = req.getParameter("patientId");
        String date = req.getParameter("date");
        String time = req.getParameter("time");
        String reason = req.getParameter("reason");
        // Newly created appointments always start with "Scheduled" status; ignore any client-submitted status
        String status = "Scheduled";
        if (isBlank(patientId)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?error=" + URLEncoder.encode("Patient is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(date)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?error=" + URLEncoder.encode("Appointment date is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(time)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?error=" + URLEncoder.encode("Appointment time is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(reason)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?error=" + URLEncoder.encode("Reason is required", StandardCharsets.UTF_8));
            return;
        }
        try {
            Appointment a = appointmentService.createAppointment(patientId.trim(), date.trim(), time.trim(), reason.trim(), status);
            String msg = URLEncoder.encode("Created ID " + a.getId(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?msg=" + msg);
        } catch (IllegalArgumentException e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?error=" + err);
        } catch (Exception e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/appointment/schedule?error=" + err);
        }
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        String patientId = req.getParameter("patientId");
        String date = req.getParameter("date");
        String time = req.getParameter("time");
        String reason = req.getParameter("reason");
        String status = req.getParameter("status");
        if (isBlank(id)) {
            resp.sendRedirect(req.getContextPath() + "/appointments?error=" + URLEncoder.encode("ID is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(patientId)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Patient is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(date)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Appointment date is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(time)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Appointment time is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(reason)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Reason is required", StandardCharsets.UTF_8));
            return;
        }
        if (isBlank(status)) {
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode("Status is required", StandardCharsets.UTF_8));
            return;
        }
        Appointment a = new Appointment(id.trim(), patientId.trim(), date.trim(), time.trim(), reason.trim(), status.trim());
        try {
            boolean ok = appointmentService.updateAppointment(a);
            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/appointments?msg=" + URLEncoder.encode("Updated", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(req.getContextPath() + "/appointments?error=" + URLEncoder.encode("Update failed: Appointment not found", StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + err);
        } catch (Exception e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/appointment/load?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "&error=" + err);
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getParameter("id");
        if (isBlank(id)) {
            resp.sendRedirect(req.getContextPath() + "/appointments?error=" + URLEncoder.encode("ID required", StandardCharsets.UTF_8));
            return;
        }
        boolean ok = appointmentService.deleteAppointment(id);
        if (ok) {
            resp.sendRedirect(req.getContextPath() + "/appointments?msg=" + URLEncoder.encode("Deleted", StandardCharsets.UTF_8));
        } else {
            resp.sendRedirect(req.getContextPath() + "/appointments?error=" + URLEncoder.encode("Delete failed", StandardCharsets.UTF_8));
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
