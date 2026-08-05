package com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components

import com.bittokazi.kvision.spa.framework.base.common.SpaAppEngine.routing
import com.bittokazi.kvision.spa.framework.base.components.form.FormSelectInput
import com.bittokazi.kvision.spa.framework.base.components.form.FormTextInput
import com.bittokazi.kvision.spa.framework.base.components.form.selectInputComponent
import com.bittokazi.kvision.spa.framework.base.components.form.textInputComponent
import com.bittokazi.kvision.spa.framework.base.components.pagination.paginationComponent
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.role.RoleService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.UserService
import com.bittokazi.oauth2.auth.frontend.frontend.app.secure.dashboard.user.components.form.UserSearchForm
import com.bittokazi.oauth2.auth.frontend.frontend.base.common.AppEngine
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.BaseList
import com.bittokazi.oauth2.auth.frontend.frontend.base.models.User
import io.kvision.core.onChange
import io.kvision.core.onInput
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.html.link
import io.kvision.html.span
import io.kvision.html.table
import io.kvision.html.tbody
import io.kvision.html.td
import io.kvision.html.th
import io.kvision.html.thead
import io.kvision.html.tr
import io.kvision.panel.SimplePanel
import io.kvision.state.ObservableValue
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
class UserListComponent : SimplePanel() {
    val userListObserver = ObservableValue<BaseList<User>?>(null)

    var page = 1
    val count = 10
    var initComplete = false

    val searchForm = mapOf(
        UserSearchForm.ROLES to FormSelectInput(
            label = "Roles",
            defaultInvalidFeedback = "At least one role must be selected",
        ) {
            return@FormSelectInput it != -1
        },
        UserSearchForm.EMAIL to FormTextInput(
            label = "Email",
            placeholder = "Search with email",
        ) {
            return@FormTextInput true
        },
        UserSearchForm.FIRST_NAME to FormTextInput(
            label = "Firstname",
            placeholder = "Search with firstname",
        ) {
            return@FormTextInput true
        },
        UserSearchForm.LAST_NAME to FormTextInput(
            label = "Lastname",
            placeholder = "Search with lastname",
        ) {
            return@FormTextInput true
        }
    )

    fun getUsers() {
        var query = ""
        if(initComplete) {
            val queryList = mutableListOf<String>()

            if (!(searchForm[UserSearchForm.ROLES] as FormSelectInput).getValue().isNullOrEmpty()) {
                queryList.add("role=${(searchForm[UserSearchForm.ROLES] as FormSelectInput).getValue()}")
            }

            if ((searchForm[UserSearchForm.EMAIL] as FormTextInput).getValue().isNotEmpty()) {
                queryList.add("email=${(searchForm[UserSearchForm.EMAIL] as FormTextInput).input.value}")
            }

            if ((searchForm[UserSearchForm.FIRST_NAME] as FormTextInput).getValue().isNotEmpty()) {
                queryList.add("firstName=${(searchForm[UserSearchForm.FIRST_NAME] as FormTextInput).input.value}")
            }

            if ((searchForm[UserSearchForm.LAST_NAME] as FormTextInput).getValue().isNotEmpty()) {
                queryList.add("lastName=${(searchForm[UserSearchForm.LAST_NAME] as FormTextInput).input.value}")
            }

            query += queryList.joinToString("+")
        }
        UserService.getAll(
            page = page,
            count = count,
            query = query,
        ).then {
            userListObserver.setState(it.data)
        }
    }

    fun tableContent(userList: BaseList<User>): Div {
        return Div(className = "row mb-3") {
            div(className = "table-responsive") {
                table(className = "table table-hover my-0") {
                    thead {
                        tr {
                            th {
                                content = "#"
                            }
                            th {
                                content = "Firstname"
                            }
                            th {
                                content = "Lastname"
                            }
                            th {
                                content = "Email"
                            }
                            th {
                                content = "Role"
                            }
                            th {
                                content = "Actions"
                            }
                        }
                    }

                    tbody {
                        userList.data.forEachIndexed { index, user ->
                            tr {
                                td {
                                    content = user.id.toString()
                                }
                                td {
                                    content = user.firstName
                                }
                                td {
                                    content = user.lastName
                                }
                                td {
                                    content = user.email
                                }
                                td {
                                    content = user.roles?.firstOrNull()?.name ?: ""
                                }
                                td {
                                    link(
                                        "",
                                        AppEngine.APP_DASHBOARD_USER_UPDATE_ROUTE(
                                            user.id!!,
                                        ),
                                        dataNavigo = true,
                                    ) {
                                        span(className = "feather-sm me-1") {
                                            setAttribute("data-feather", "edit")
                                        }
                                        +" Edit"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun pagination(userList: BaseList<User>): Div {
        return div(className = "row") {
            add(
                paginationComponent(
                    when (page > 1) {
                        true -> page - 1
                        false -> null
                    },
                    when (page < userList.totalPage.toInt()) {
                        true -> page + 1
                        false -> null
                    },
                    page,
                    userList.totalPage.toInt(),
                    userList.totalCount.toInt(),
                    count,
                ) { pageNumber ->
                    page = pageNumber
                    getUsers()
                },
            )
        }
    }

    init {
        div {
            RoleService.getAll().then { roleList ->
                div(className = "row") {
                    add(
                        div(className = "col-md-3") {
                            add(
                                selectInputComponent(
                                    (searchForm[UserSearchForm.ROLES] as FormSelectInput),
                                    listOf("" to "Any") + roleList.data.map { it.roleKey.toString() to it.name!! },
                                    "",
                                ),
                            )
                        }
                    )

                    add(
                        div(className = "col-md-3") {
                            add(
                                textInputComponent(
                                    (searchForm[UserSearchForm.EMAIL] as FormTextInput),
                                    "",
                                    false,
                                ),
                            )
                        }
                    )

                    add(
                        div(className = "col-md-3") {
                            add(
                                textInputComponent(
                                    (searchForm[UserSearchForm.FIRST_NAME] as FormTextInput),
                                    "",
                                    false,
                                ),
                            )
                        }
                    )

                    add(
                        div(className = "col-md-3") {
                            add(
                                textInputComponent(
                                    (searchForm[UserSearchForm.LAST_NAME] as FormTextInput),
                                    "",
                                    false,
                                ),
                            )
                        }
                    )
                }

                (searchForm[UserSearchForm.ROLES] as FormSelectInput).input.onChange {
                    getUsers()
                }
                (searchForm[UserSearchForm.EMAIL] as FormTextInput).input.onInput {
                    validateTextSearchInput(searchForm[UserSearchForm.EMAIL] as FormTextInput)
                }
                (searchForm[UserSearchForm.FIRST_NAME] as FormTextInput).input.onInput {
                    validateTextSearchInput(searchForm[UserSearchForm.FIRST_NAME] as FormTextInput)
                }
                (searchForm[UserSearchForm.LAST_NAME] as FormTextInput).input.onInput {
                    validateTextSearchInput(searchForm[UserSearchForm.LAST_NAME] as FormTextInput)
                }
                initComplete = true
            }
        }
        div {
            userListObserver.subscribe {
                if (it != null) {
                    removeAll()
                    add(tableContent(it))
                    add(pagination(it))

                    routing.updatePageLinks()

                    window.setTimeout({
                        window["feather"].replace()
                    }, 100)
                }
            }
            getUsers()
        }
    }

    private fun validateTextSearchInput(input: FormTextInput) {
        if (input.getValue().length > -1) {
            getUsers()
        }
    }
}
