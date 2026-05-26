import {createLogger, createStore} from 'vuex';

import student from "./modules/entities/student";
import user from "./modules/entities/user";
import course from "./modules/entities/course";
import enrollment from "./modules/entities/enrollment";
import assignment from "./modules/entities/assignment";
import professor from "./modules/entities/professor";
import program from "./modules/entities/program";
const debug = process.env.NODE_ENV !== 'production';

export default createStore({
  strict: debug,
  plugins: debug ? [createLogger()] : [],
  modules: {
    user,
    student,
    course,
    professor,
    program,
    assignment,
    enrollment,

  }
});