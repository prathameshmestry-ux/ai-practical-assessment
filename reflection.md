# Reflection
## What I Built  
For this project, I developed a custom Support Ticket Management System in AEM that genuinely feels like a snappy, modern web app rather than a traditional static site. I wanted the dashboard to be highly interactive, so I used asynchronous JavaScript and AEM's QueryBuilder to handle live searches and inline ticket updates without annoying page reloads. Leaning on an AI co-pilot while sticking to a strict spec-driven approach made the whole process incredibly smooth, letting me focus on the actual architecture instead of getting bogged down in boilerplate.
## How I Used AI (across the lifecycle)
I treated AI as a true engineering co-pilot rather than just a code generator, starting with our spec-driven approach to map out the architecture before touching any code. Once the blueprints were locked, I used it to quickly scaffold the tedious AEM boilerplate like Sling Models, XML dialogs, and HTL templates. It really shined as a sounding board when I hit complex roadblocks, like refactoring slow JCR queries into native UserManager calls. Having that constant peer review kept the architecture clean and let me focus entirely on solving the core business logic.
## What AI Helped With Most  
Instead of burning hours digging how to implement Material UI, AI helped me implement that quickly. Also, I could bounce ideas around and get solid architectural fixes immediately.
## What AI Got Wrong
- The UI was completely out of place. What tripped me up most were AEM platform details, not Java syntax. AI kept mixing up pages (/content/.../support-tickets) with ticket data (/var/.../tickets) — so docs and early fixes pointed at the wrong CRXDE path until I verified nodes myself. Writes failed with 403 until ticket-service had rep:policy; AI talked servlet code first, but ACL was the real blocker.
## How I Validated AI Output
I validated the AI’s work after every deployment, I loaded up the Touch UI to ensure the generated components dragged and dropped smoothly, and that the dialogs actually saved content to the JCR without glitching. On the backend, I strictly cross-checked its Java logic against my original plan.md to guarantee it wasn't hallucinating features or drifting from our core architecture. Whenever an issue popped up, I just fed the AEM stack traces or CRXDE node behaviors straight back into the chat to force a quick and accurate course correction.  
## What I Would Improve Next
I want to get into the habit of asking it to generate JUnit test cases alongside the Sling Models, rather than treating testing as an afterthought. I also want to proactively prompt it to generate failure scenarios and edge cases upfront.

## Reusable Workflow (prompts, rules, specs, templates)
1. Spec Kit workflow (biggest reuse)
Same loop works on any feature:
`Constitution → Specify → Clarify → Plan → Tasks → Implement → Quickstart`
2. Prompt pattern (reusable)
Start every feature with a clear prompt:

- What to build (user journeys)
- Constraints (platform, auth, storage)
- Out of scope (v1 boundaries)
3. Rules pattern (adapt per project)
Reuse the structure of `.specify/memory/constitution.md`
4. SDD Spec folder layout (reusable) Per feature
specs/###-feature-name/
  spec.md          
  plan.md          
  research.md      
  data-model.md    
  tasks.md         
  quickstart.md    
  contracts/       
  checklists/ 