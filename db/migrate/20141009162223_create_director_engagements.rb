Sequel.migration do
  up do
    create_table(:director_engagements) do
      primary_key :id

      foreign_key :person_id, :people, :foreign_key_constraint_name => 'director_engagement_fkey_person_id', :null => false
      foreign_key :project_id, :projects, :foreign_key_constraint_name => 'director_engagement_fkey_project_id', :null => false

      DateTime :start
      DateTime :end

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:director_engagements)
  end
end
