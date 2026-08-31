**Primary AI tool used.**
Cursor

- **How you provide project context to the tool.**  
I maintain core documentation files (constitution.md, spec.md, plan.md) that act as the single source of truth, dictating overarching rules like AEM Touch UI standards, JCR path structures, and global formatting mandates before asking the tool to generate any code.
- **How you use AI for requirement analysis.**
I feed raw feature ideas into the AI and prompt it to map out comprehensive functional requirements and edge cases, such as identifying the need for granular, component-level authentication checks rather than relying solely on global OSGi redirects.  
- **How you use AI for planning and design.**  
Following a Specification-Driven Development (SDD) methodology, I use the AI to translate refined specs into a strict technical blueprint, defining exact Sling Models, Servlet endpoints, Dispatcher cache rules, and data persistence hierarchies (e.g., nesting comments under `/var/ai-practical-assessment/tickets`).
- **How you use AI for code generation.**  
I direct the AI to generate modular AEM artifacts—like HTL scripts, `cq:dialog` XML, ClientLib JavaScript for asynchronous fetches, and Java Servlets—strictly bound to the approved technical plan to prevent hallucinations.
- **How you validate AI-generated code.**  
I deploy the generated packages to a local AEM instance, manually verify the JCR node types (ensuring components deploy as `cq:Component` and not `nt:folder`) via CRXDE Lite, and check the Package Manager logs for XML syntax failures.
- **How you use AI for testing.**  
I prompted the tool to generate structured unit tests for backend Java logic or to define step-by-step manual testing criteria, such as verifying that asynchronous DOM updates function seamlessly without triggering a full page reload.
- **How you use AI for debugging.**  
I feed specific error logs back into the AI to diagnose structural flaws like missing `cq:editConfig` nodes, instructing it to update the architectural plan before regenerating the fix.
- **How you use AI for code review.**  
I use the AI as a peer reviewer to audit my implementation against AEM best practices, successfully leveraging it to refactor slow JCR-SQL2 queries into highly optimized Jackrabbit `UserManager` API iterations.
- **What information you avoid sharing unnecessarily with AI tools.**  
I strictly exclude production credentials, proprietary corporate logic, PII (Personally Identifiable Information), and internal network infrastructure details, relying entirely on generalized repository paths and dummy data.
- **How you would reuse this workflow in a real project.**  
I will enforce this exact SDD approach across future software builds by treating the documentation as the primary interface with the AI—mandating a "fix the docs, then the code" protocol to maintain a pristine, scalable architecture.

