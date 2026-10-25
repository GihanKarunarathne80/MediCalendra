<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Appointment List - MediCalendra</title>
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
                <h1>Appointments List</h1>
                <p style="margin:4px 0 0 0; color:#64748b; font-size:0.95rem;">Manage scheduled appointments, track statuses, and view booking history.</p>
            </div>
            <a href="${pageContext.request.contextPath}/appointment/schedule" class="btn primary">
                <img src="${pageContext.request.contextPath}/assets/icons/calendar-plus.svg" alt="" class="action-icon" width="19" height="19" />
                <span>Schedule New Appointment</span>
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
        <section class="table-section">
            <h2>All Appointments</h2>
            <form method="get" action="${pageContext.request.contextPath}/appointment/search" class="search-form">
                <input type="text" name="q" placeholder="Search appointments..." value="${searchQuery}" />
                <button type="submit" class="btn">
                    <img src="${pageContext.request.contextPath}/assets/icons/search.svg" alt="" class="action-icon" width="19" height="19" />
                    <span>Search</span>
                </button>
                <a href="${pageContext.request.contextPath}/appointments" class="btn secondary">Clear</a>
            </form>
            <c:choose>
                <c:when test="${not empty appointments}">
                    <div class="data-table-container">
                    <table class="data-table">
                        <thead>
                            <tr><th>ID</th><th>Patient</th><th>Date</th><th>Time</th><th>Reason</th><th>Status</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="a" items="${appointments}">
                                <tr>
                                    <td><strong><c:out value="${a.id}"/></strong></td>
                                    <td>
                                        <c:set var="matchedPatientName" value="" />
                                        <c:forEach var="p" items="${patients}">
                                            <c:if test="${p.id == a.patientId}"><c:set var="matchedPatientName" value="${p.fullName}" /></c:if>
                                        </c:forEach>
                                        <c:out value="${not empty matchedPatientName ? matchedPatientName : a.patientId}" />
                                    </td>
                                    <td><c:out value="${a.date}"/></td>
                                    <td><c:out value="${a.time}"/></td>
                                    <td><c:out value="${a.reason}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${a.status == 'Scheduled'}">
                                                <span class="badge badge-scheduled">Scheduled</span>
                                            </c:when>
                                            <c:when test="${a.status == 'Completed'}">
                                                <span class="badge badge-completed">Completed</span>
                                            </c:when>
                                            <c:when test="${a.status == 'Cancelled'}">
                                                <span class="badge badge-cancelled">
                                                    <img src="${pageContext.request.contextPath}/assets/icons/calendar-x.svg" alt="" class="table-action-icon" width="13" height="13" style="width:13px; height:13px;" />
                                                    <span>Cancelled</span>
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge"><c:out value="${a.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="actions-cell">
                                        <a href="${pageContext.request.contextPath}/appointment/load?id=${a.id}" class="btn small secondary">
                                            <img src="${pageContext.request.contextPath}/assets/icons/edit.svg" alt="" class="table-action-icon" width="17" height="17" />
                                            <span>Edit</span>
                                        </a>
                                        <form method="post" action="${pageContext.request.contextPath}/appointment/delete" style="display:inline;" onsubmit="return confirm('Delete this appointment?');">
                                            <input type="hidden" name="id" value="${a.id}" />
                                            <button type="submit" class="btn danger small">
                                                <img src="${pageContext.request.contextPath}/assets/icons/trash.svg" alt="" class="table-action-icon" width="17" height="17" />
                                                <span>Delete</span>
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p style="padding:24px 0; color:#64748b; text-align:center;">No appointments found.</p>
                </c:otherwise>
            </c:choose>
        </section>
    </div>
</body>
</html>

