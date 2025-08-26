
```
gestion-remodelacion
├─ .angular
├─ .editorconfig
├─ angular.json
├─ estructura_carpetas.txt
├─ package-lock.json
├─ package.json
├─ public
│  └─ favicon.ico
├─ README.md
├─ src
│  ├─ app
│  │  ├─ app.component.html
│  │  ├─ app.component.scss
│  │  ├─ app.component.ts
│  │  ├─ app.config.ts
│  │  ├─ app.routes.ts
│  │  ├─ core
│  │  │  ├─ guards
│  │  │  │  ├─ auth.guard.ts
│  │  │  │  ├─ role.guard.ts
│  │  │  │  └─ valid-token.guard.ts
│  │  │  ├─ interceptors
│  │  │  │  ├─ error.interceptor.ts
│  │  │  │  └─ jwt.interceptor.ts
│  │  │  ├─ material.module.ts
│  │  │  ├─ models
│  │  │  │  ├─ dashboard-summary.model.ts
│  │  │  │  ├─ page.model.ts
│  │  │  │  ├─ role.model.ts
│  │  │  │  └─ user.model.ts
│  │  │  └─ services
│  │  │     └─ auth.service.ts
│  │  ├─ modules
│  │  │  ├─ auth
│  │  │  │  ├─ auth.module.ts
│  │  │  │  ├─ components
│  │  │  │  │  └─ login
│  │  │  │  │     ├─ login.component.html
│  │  │  │  │     ├─ login.component.scss
│  │  │  │  │     └─ login.component.ts
│  │  │  │  ├─ models
│  │  │  │  │  ├─ auth-response.model.ts
│  │  │  │  │  ├─ login-request.model.ts
│  │  │  │  │  ├─ login-response.model.ts
│  │  │  │  │  ├─ refresh-token-request.model.ts
│  │  │  │  │  └─ signup-request.model.ts
│  │  │  │  └─ services
│  │  │  │     └─ auth-api.service.ts
│  │  │  ├─ cliente
│  │  │  │  ├─ cliente.module.ts
│  │  │  │  ├─ components
│  │  │  │  │  ├─ cliente-form
│  │  │  │  │  │  ├─ cliente-form.component.html
│  │  │  │  │  │  ├─ cliente-form.component.scss
│  │  │  │  │  │  └─ cliente-form.component.ts
│  │  │  │  │  └─ cliente-list
│  │  │  │  │     ├─ cliente-list.component.html
│  │  │  │  │     ├─ cliente-list.component.scss
│  │  │  │  │     └─ cliente-list.component.ts
│  │  │  │  ├─ models
│  │  │  │  │  └─ cliente.model.ts
│  │  │  │  └─ services
│  │  │  │     └─ cliente.service.ts
│  │  │  ├─ dashboard
│  │  │  │  ├─ components
│  │  │  │  │  └─ dashboard
│  │  │  │  │     ├─ dashboard.component.html
│  │  │  │  │     ├─ dashboard.component.scss
│  │  │  │  │     └─ dashboard.component.ts
│  │  │  │  ├─ models
│  │  │  │  │  └─ dashboard-proyecto.model.ts
│  │  │  │  └─ services
│  │  │  │     └─ dashboard.service.ts
│  │  │  ├─ empleados
│  │  │  │  ├─ components
│  │  │  │  │  ├─ empleado-form
│  │  │  │  │  │  ├─ empleado-form.component.html
│  │  │  │  │  │  ├─ empleado-form.component.scss
│  │  │  │  │  │  └─ empleado-form.component.ts
│  │  │  │  │  └─ empleado-list
│  │  │  │  │     ├─ empleado-list.component.html
│  │  │  │  │     ├─ empleado-list.component.scss
│  │  │  │  │     └─ empleado-list.component.ts
│  │  │  │  ├─ empleados.module.ts
│  │  │  │  ├─ models
│  │  │  │  │  └─ empleado.model.ts
│  │  │  │  └─ services
│  │  │  │     └─ empleado.service.ts
│  │  │  ├─ proyectos
│  │  │  │  ├─ components
│  │  │  │  │  ├─ proyecto-form
│  │  │  │  │  │  ├─ proyectos-form.component.html
│  │  │  │  │  │  ├─ proyectos-form.component.scss
│  │  │  │  │  │  └─ proyectos-form.component.ts
│  │  │  │  │  └─ proyecto-list
│  │  │  │  │     ├─ proyecto-list.component.html
│  │  │  │  │     ├─ proyecto-list.component.scss
│  │  │  │  │     └─ proyecto-list.component.ts
│  │  │  │  ├─ models
│  │  │  │  │  └─ proyecto.model.ts
│  │  │  │  ├─ proyectos.module.ts
│  │  │  │  └─ services
│  │  │  │     └─ proyecto.service.ts
│  │  │  └─ reportes
│  │  │     ├─ components
│  │  │     │  └─ reporte-list
│  │  │     │     ├─ reporte-list.component.html
│  │  │     │     ├─ reporte-list.component.scss
│  │  │     │     └─ reporte-list.component.ts
│  │  │     ├─ models
│  │  │     │  └─ reporte.model.ts
│  │  │     └─ services
│  │  │        └─ reporte.service.ts
│  │  └─ shared
│  │     ├─ components
│  │     │  ├─ footer
│  │     │  │  ├─ footer.component.html
│  │     │  │  ├─ footer.component.scss
│  │     │  │  └─ footer.component.ts
│  │     │  ├─ header
│  │     │  │  ├─ header.component.html
│  │     │  │  ├─ header.component.scss
│  │     │  │  └─ header.component.ts
│  │     │  ├─ layout
│  │     │  │  ├─ layout.component.html
│  │     │  │  ├─ layout.component.scss
│  │     │  │  └─ layout.component.ts
│  │     │  ├─ navbar
│  │     │  │  ├─ navbar.component.html
│  │     │  │  ├─ navbar.component.scss
│  │     │  │  └─ navbar.component.ts
│  │     │  └─ sidebar
│  │     │     ├─ sidebar.component.html
│  │     │     ├─ sidebar.component.scss
│  │     │     ├─ sidebar.component.ts
│  │     │     └─ sidebar.service.ts
│  │     ├─ directives
│  │     │  └─ highlight.directive.ts
│  │     ├─ models
│  │     │  └─ menu-item.model.ts
│  │     ├─ pipes
│  │     │  └─ capitalize.pipe.ts
│  │     └─ utils
│  │        └─ constants.ts
│  ├─ assets
│  │  ├─ data
│  │  │  └─ empleados.json
│  │  ├─ images
│  │  │  └─ logo.png
│  │  └─ styles
│  │     └─ theme.css
│  ├─ environments
│  │  ├─ environment.prod.ts
│  │  └─ environment.ts
│  ├─ index.html
│  ├─ main.ts
│  ├─ styles
│  │  ├─ global.css
│  │  └─ variables.css
│  └─ styles.scss
├─ tsconfig.app.json
├─ tsconfig.json
└─ tsconfig.spec.json

```