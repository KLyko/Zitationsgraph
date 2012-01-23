package de.uni.leipzig.asv.zitationsgraph.tests.vis;

import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.DragForce;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RungeKuttaIntegrator;
import prefuse.util.force.SpringForce;



	public class SpringLayout extends ActionList{

		private ForceDirectedLayout spring;
		
		protected SpringLayout() {
			
			
			super();
			// TODO Auto-generated constructor stub
			spring=new ForceDirectedLayout(PubVis.GRAPH, true, false);
			
			//spring.setMaxTimeStep(200);
			spring.getForceSimulator().setIntegrator(new RungeKuttaIntegrator());
			spring.getForceSimulator().addForce(new DragForce((float)0.000000001));
			spring.getForceSimulator().addForce(new SpringForce((float) 0.0001, 250));
			spring.getForceSimulator().addForce(new NBodyForce((float)0.000001, (float)100, (float)0.35 ));
			this.add(spring);
			this.add(new RepaintAction());
			
			
		}
	
}
