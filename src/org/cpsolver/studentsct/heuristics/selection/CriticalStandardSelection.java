package org.cpsolver.studentsct.heuristics.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.ValueSelection;
import org.cpsolver.ifs.heuristics.VariableSelection;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;

/**
 * Use the standard IFS search for the unassigned critical course requests.
 * This selection is based on {@link StandardSelection} using
 * {@link UnassignedCriticalCourseRequestSelection} as variable selection.
 * Unlike {@link StandardSelection}, it allows for a critical course request to be
 * unassigned.
 * 
 * @version StudentSct 1.3 (Student Sectioning)<br>
 *          Copyright (C) 2007 - 2014 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class CriticalStandardSelection extends StandardSelection {
    
    public CriticalStandardSelection(DataProperties properties, ValueSelection<Request, Enrollment> valueSelection) {
        super(properties, new UnassignedCriticalCourseRequestSelection(), valueSelection);
    }
    
    @Override
    public void init(Solver<Request, Enrollment> solver) {
        init(solver, "Ifs (critical)...");
    }
    
    @Override
    public boolean canUnassign(Enrollment enrollment, Enrollment conflict, Assignment<Request, Enrollment> assignment) {
        if (conflict.getRequest().isMPP() && conflict.equals(conflict.getRequest().getInitialAssignment())) return false;
        // Override to allow unassignment of critical course requests
        return true;
    }
    
    /**
     * Returns the unassigned critical course requests in a random order.
     */
    static class UnassignedCriticalCourseRequestSelection implements VariableSelection<Request, Enrollment>{
        protected int iNrRounds = 0;
        protected Queue<Request> iRequests = null;
        
        @Override
        public void init(Solver<Request, Enrollment> solver) {
            iRequests = new LinkedList<Request>();
            iNrRounds = solver.getProperties().getPropertyInt("Neighbour.CriticalRounds", 10);
        }

        @Override
        public Request selectVariable(Solution<Request, Enrollment> solution) {
            return nextRequest(solution);
        }
        
        protected synchronized Request nextRequest(Solution<Request, Enrollment> solution) {
            if (iRequests.isEmpty() && iNrRounds > 0) {
                iNrRounds --;
                List<Request> variables = new ArrayList<Request>();
                for (Request r: solution.getModel().unassignedVariables(solution.getAssignment()))
                    if (r.isCritical()) variables.add(r);
                Collections.shuffle(variables);
                iRequests.addAll(variables);
            }
            return iRequests.poll();
        }
    }
}