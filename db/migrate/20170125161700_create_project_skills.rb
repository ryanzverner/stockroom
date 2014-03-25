Sequel.migration do
  up do
    create_table(:project_skills) do
      primary_key :id

      foreign_key :project_id, :projects, :foreign_key_constraint_name => 'project_skills_fkey_project_id'
      foreign_key :skill_id, :skills, :foreign_key_constraint_name => 'project_skills_fkey_skill_id'

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:project_skills)
  end
end
