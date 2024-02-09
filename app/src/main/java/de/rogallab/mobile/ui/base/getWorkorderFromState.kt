package de.rogallab.mobile.ui.base

import de.rogallab.mobile.domain.entities.Workorder

class getWorkorderFromState {
}

fun getWorkorderFromState(workorderStateValue: Workorder): Workorder =
   Workorder(
      title = workorderStateValue.title,
      description = workorderStateValue.description,
      imagePath = workorderStateValue.imagePath,
      state = workorderStateValue.state,
      created = workorderStateValue.created,
      started = workorderStateValue.started,
      completed = workorderStateValue.completed,
      duration = workorderStateValue.duration,
      remark = workorderStateValue.remark,
      id = workorderStateValue.id,
      person = workorderStateValue.person,
      personId = workorderStateValue.personId
   )
