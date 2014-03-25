Sequel.migration do
  up do
    create_table(:engagements) do
      primary_key :id
      Date :start
      Date :end

      foreign_key :project_id, :projects, :foreign_key_constraint_name => 'engagements_fkey_project_id'
      foreign_key :employment_id, :employment, :foreign_key_constraint_name => 'engagements_fkey_employment_id'

      DateTime :updated_at
      DateTime :created_at
    end
  end

  down do
    drop_table(:engagements)
  end
end
