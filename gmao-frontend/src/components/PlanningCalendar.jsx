import React from 'react'
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import listPlugin from '@fullcalendar/list'
import { useInterventions, useUpdateDates } from '../hooks/useInterventions'
import { toCalendarEvents } from '../utils/calendarHelpers'
import './PlanningCalendar.css' // 🟢 Import du CSS isolé

export default function PlanningCalendar({ filters }) {
  const { data: interventions = [], isLoading } = useInterventions()
  const mutateDates = useUpdateDates()

  const handleEventDrop = (info) => {
    const id = info.event.id
    const newStart = info.event.startStr
    const newEnd = info.event.endStr || null

    mutateDates.mutate({
      id: parseInt(id),
      payload: {
        dateDebut: newStart.split('+')[0],
        dateFin: newEnd ? newEnd.split('+')[0] : null
      }
    })
  }

  if (isLoading) {
    return <div className="calendar-loading">Chargement du planning...</div>
  }

  const events = toCalendarEvents(interventions, filters.colorMode)

  return (
    <div className="calendar-wrapper glass anim-fadein">
      <FullCalendar
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin, listPlugin]}
        initialView="timeGridWeek"
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
        }}
        locale="fr"
        firstDay={1}
        events={events}
        editable={true}
        droppable={true}
        eventDrop={handleEventDrop}
        eventResize={handleEventDrop}
        slotMinTime="07:00:00"
        slotMaxTime="19:00:00"
        height="650px"
      />
    </div>
  )
}