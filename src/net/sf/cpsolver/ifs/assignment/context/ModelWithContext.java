package net.sf.cpsolver.ifs.assignment.context;

import net.sf.cpsolver.ifs.assignment.Assignment;
import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.model.Variable;

/**
 * A model with an assignment context. In order to be able to hold multiple assignments in memory
 * it is desired for all the assignment dependent data a constraint may need (to effectively enumerate
 * problem objectives), to store these data in a separate class (implementing the 
 * {@link AssignmentConstraintContext} interface). This context is created by calling
 * {@link ConstraintWithContext#createAssignmentContext(Assignment)} and accessed by
 * {@link ConstraintWithContext#getContext(Assignment)}.
 * 
 * 
 * @see AssignmentContext
 * 
 * @version IFS 1.2 (Iterative Forward Search)<br>
 *          Copyright (C) 2014 Tomas Muller<br>
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
 *          License along with this library; if not see <http://www.gnu.org/licenses/>.
 **/
public abstract class ModelWithContext<V extends Variable<V, T>, T extends Value<V, T>, C extends AssignmentConstraintContext<V, T>> extends Model<V, T> implements HasAssignmentContext<V, T, C>, CanHoldContext {
    
    private AssignmentContextReference<V, T, C> iContextReference = null;
    private AssignmentContext[] iContext = null;
    
    public ModelWithContext() {
        super();
        iContextReference = createReference(this);
    }
    
    /**
     * Returns an assignment context associated with this model. If there is no 
     * assignment context associated with this model yet, one is created using the
     * {@link ConstraintWithContext#createAssignmentContext(Assignment)} method. From that time on,
     * this context is kept with the assignment and automatically updated by calling the
     * {@link AssignmentConstraintContext#assigned(Assignment, Value)} and {@link AssignmentConstraintContext#unassigned(Assignment, Value)}
     * whenever a variable is changed.
     * @param assignment given assignment
     * @return assignment context associated with this model and the given assignment
     */
    @SuppressWarnings("unchecked")
    public C getContext(Assignment<V, T> assignment) {
        if (iContext != null && assignment.getIndex() >= 0 && assignment.getIndex() < iContext.length) {
            AssignmentContext c = iContext[assignment.getIndex()];
            if (c != null) return (C)c;
        }
        return assignment.getAssignmentContext(getAssignmentContextReference());
    }

    @Override
    public AssignmentContextReference<V, T, C> getAssignmentContextReference() { return iContextReference; }

    @Override
    public void setAssignmentContextReference(AssignmentContextReference<V, T, C> reference) { iContextReference = reference; }

    @Override
    public AssignmentContext[] getContext() { return iContext; }

    @Override
    public void setContext(AssignmentContext[] context) { iContext = context; }
    
    @Override
    public void beforeUnassigned(Assignment<V, T> assignment, long iteration, T value) {
        super.beforeUnassigned(assignment, iteration, value);
        getContext(assignment).unassigned(assignment, value);
    }
    
    @Override
    public void afterAssigned(Assignment<V, T> assignment, long iteration, T value) {
        super.afterAssigned(assignment, iteration, value);
        getContext(assignment).assigned(assignment, value);
    }

}