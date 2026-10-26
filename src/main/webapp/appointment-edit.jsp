<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Update Appointment - MediCalendra</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
    <script src="${pageContext.request.contextPath}/js/appointments.js" defer></script>
</head>
<body>
    <aside class="sidebar">
        <div class="sidebar-brand">
            <div class="sidebar-brand-icon">
                <img src="${pageContext.request.contextPath}/assets/icons/logo-heart-pulse.svg" alt="MediCalendra Logo" class="brand-icon" width="40" height="40" />
            </div>
            <div class="sidebar-brand-text">
                <h2>MediCalendra</h2>
                <span>Healthcare Portal</span>
            </div>
            <img src="${pageContext.request.contextPath}/assets/icons/menu.svg" alt="Toggle Menu" class="menu-icon" width="22" height="22" />
        </div>
        <nav>
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/">
                        <img src="${pageContext.request.contextPath}/assets/icons/dashboard.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Dashboard</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/patient/register">
                        <img src="${pageContext.request.contextPath}/assets/icons/user-plus.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Patient Registration</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/patients">
                        <img src="${pageContext.request.contextPath}/assets/icons/users.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Patient List</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/appointment/schedule">
                        <img src="${pageContext.request.contextPath}/assets/icons/calendar-plus.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Schedule Appointment</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/appointments" class="active">
                        <img src="${pageContext.request.contextPath}/assets/icons/calendar-days.svg" alt="" class="nav-icon" width="21" height="21" />
                        <span>Appointment List</span>
                    </a>
                </li>
            </ul>
        </nav>
    </aside>
    <div class="main-content">
        <div class="page-header">
            <div>
                <h1>Update Appointment</h1>
                <p style="margin:4px 0 0 0; color:#64748b; font-size:0.95rem;">Modify details for appointment record <strong><c:out value="${editAppointment.id}"/></strong>.</p>
            </div>
            <a href="${pageContext.request.contextPath}/appointments" class="btn secondary">
                <img src="${pageContext.request.contextPath}/assets/icons/calendar-days.svg" alt="" class="action-icon" width="19" height="19" />
                <span>Back to Appointment List</span>
            </a>
        </div>
        <c:if test="${not empty param.msg}">
            <div class="msg success">
                <img src="${pageContext.request.contextPath}/assets/icons/check-circle.svg" alt="" class="action-icon" width="19" height="19" />
                <span><c:out value="${param.msg}"/></span>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="msg error"><c:out value="${param.error}"/></div>
        </c:if>
        <section class="form-section">
            <h2>Edit Appointment Details</h2>
            <form method="post" action="${pageContext.request.contextPath}/appointment/edit">
                <input type="hidden" name="id" value="${editAppointment.id}" />
                <div class="form-grid">
                    <div class="form-group">
                        <label for="patientId">Patient</label>
                        <select id="patientId" name="patientId" required>
                            <option value="">-- Select Patient --</option>
                            <c:forEach var="p" items="${patients}">
                                <option value="${p.id}" ${p.id == editAppointment.patientId ? 'selected' : ''}>${p.fullName} (${p.id})</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="status">Status</label>
                        <select id="status" name="status" required>
                            <c:forEach var="s" items="${['Scheduled','Completed','Cancelled']}">
                                <option value="${s}" ${s == editAppointment.status ? 'selected' : ''}>${s}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="date">Appointment Date</label>
                        <input type="date" id="date" name="date" value="${editAppointment.date}" required />
                    </div>
                    <div class="form-group">
                        <label for="time">Appointment Time</label>
                        <input type="time" id="time" name="time" value="${editAppointment.time}" required />
                    </div>
                    <div class="form-group full-width">
                        <label for="reason">Reason for Appointment</label>
                        <input type="text" id="reason" name="reason" value="${editAppointment.reason}" placeholder="e.g. General check-up" required />
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn primary">
                        <img src="${pageContext.request.contextPath}/assets/icons/edit.svg" alt="" class="action-icon" width="19" height="19" />
                        <span>Update Appointment</span>
                    </button>
                    <a href="${pageContext.request.contextPath}/appointments" class="btn secondary">Cancel</a>
                </div>
            </form>
        </section>
    </div>
</body>
</html>

